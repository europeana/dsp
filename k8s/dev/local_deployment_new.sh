#!/bin/bash
#ENSURE Linux EOL
set -e

# ============================================================
# Configuration
# ============================================================

IMAGE_TAG="${1:-$(git rev-parse --abbrev-ref HEAD)}"
IMAGE_TAG="${IMAGE_TAG#*/}"

K8S_DIR="k8s/dev"
TMP_DIR=$(mktemp -d)

echo ""
echo "========================================"
echo "Deploying EDC DEV environment"
echo "========================================"
echo "Image tag : ${IMAGE_TAG}"
echo "========================================"
echo ""

# ============================================================
# Cleanup temporary directory on exit
# ============================================================

trap 'rm -rf "${TMP_DIR}"' EXIT

# ============================================================
# Check prerequisites
# ============================================================

if ! command -v kubectl >/dev/null 2>&1; then
    echo "ERROR: kubectl is not installed or not in PATH"
    exit 1
fi

if ! command -v kustomize >/dev/null 2>&1; then
    echo "ERROR: kustomize is not installed or not in PATH"
    exit 1
fi

if [ ! -f "${K8S_DIR}/kustomization.yml" ]; then
    echo "ERROR: ${K8S_DIR}/kustomization.yml not found"
    exit 1
fi

# ============================================================
# Check Kubernetes connection
# ============================================================

echo "Checking Kubernetes connection..."

if ! kubectl cluster-info >/dev/null 2>&1; then
    echo "ERROR: Cannot connect to Kubernetes"
    exit 1
fi

echo "Kubernetes connection OK"

# ============================================================
# Copy DEV Kubernetes configuration
# ============================================================

echo ""
echo "Preparing DEV Kubernetes configuration..."

cp -R "${K8S_DIR}/." "${TMP_DIR}/"

cd "${TMP_DIR}"

# ============================================================
# Set local Docker images
#
# These changes happen only in TMP_DIR.
# The real k8s/dev files are not modified.
# ============================================================

echo ""
echo "Setting local Docker images..."

kustomize edit set image dsp-controlplane="europeana/dsp-controlplane:${IMAGE_TAG}"
#kustomize edit set image dsp-issuerservice="europeana/dsp-issuerservice:${IMAGE_TAG}"
#kustomize edit set image dsp-identity-hub="europeana/dsp-identity-hub:${IMAGE_TAG}"
#kustomize edit set image dsp-dataplane="europeana/dsp-dataplane:${IMAGE_TAG}"

# ============================================================
# Show images that will be deployed
# ============================================================

echo ""
echo "========================================"
echo "Images to deploy"
echo "========================================"

kustomize build .

# ============================================================
# Deploy to Kubernetes
# ============================================================

echo ""
echo "========================================"
echo "Applying Kubernetes configuration"
echo "========================================"

kubectl apply -k .
 

# ============================================================
# Wait for deployments
# ============================================================

echo ""
echo "========================================"
echo "Waiting for deployments"
echo "========================================"

DEPLOYMENTS=(
    "consumer/controlplane"
    "consumer/dataplane"
    "consumer/identityhub"
    "provider/controlplane"
    "provider/dataplane"
    "provider/identityhub"
    "issuer/issuerservice"
)

DEPLOYMENT_FAILED=false

for DEPLOYMENT in "${DEPLOYMENTS[@]}"; do

    NAMESPACE="${DEPLOYMENT%%/*}"
    NAME="${DEPLOYMENT##*/}"

    echo ""
    echo "Waiting for ${NAME} in namespace ${NAMESPACE}..."

    if kubectl rollout status \
        "deployment/${NAME}" \
        -n "${NAMESPACE}" \
        --timeout=500s; then

        echo "✓ ${NAME} deployed successfully"

    else

        echo "✗ ${NAME} deployment failed"
        DEPLOYMENT_FAILED=true

    fi

done

# ============================================================
# Show deployment status
# ============================================================

echo ""
echo "========================================"
echo "Deployment status"
echo "========================================"

echo ""
echo "Consumer namespace:"
kubectl get pods -n consumer

echo ""
echo "Issuer namespace:"
kubectl get pods -n issuer

# ============================================================
# Show images actually running
# ============================================================

echo ""
echo "========================================"
echo "Images running in Kubernetes"
echo "========================================"

echo "mvd-common namespace:"

kubectl get pods -n mvd-common \
    -o custom-columns='NAME:.metadata.name,IMAGE:.spec.containers[*].image'


echo "Issuer namespace:"

kubectl get pods -n issuer \
    -o custom-columns='NAME:.metadata.name,IMAGE:.spec.containers[*].image'

echo ""
echo "Consumer namespace:"

kubectl get pods -n consumer \
    -o custom-columns='NAME:.metadata.name,IMAGE:.spec.containers[*].image'

echo ""

echo "Provider namespace:"

kubectl get pods -n provider \
    -o custom-columns='NAME:.metadata.name,IMAGE:.spec.containers[*].image'


# ============================================================
# Deployment result
# ============================================================

if [ "${DEPLOYMENT_FAILED}" = true ]; then

    echo ""
    echo "========================================"
    echo "EDC DEV DEPLOYMENT FAILED"
    echo "========================================"

    echo ""
    echo "Check the failing pods with:"
    echo ""
    echo "kubectl get pods -n consumer"
    echo "kubectl get pods -n issuer"
    echo ""

    exit 1
fi


# wait for all init jobs to have completed
kubectl wait -A \
  --selector=type=edc-job \
  --for=condition=complete job --all \
  --timeout=180s


echo ""
echo "========================================"
echo "EDC DEV DEPLOYMENT SUCCESSFUL"
echo "========================================"
echo ""
echo "Image tag: ${IMAGE_TAG}"
echo ""

echo ""
echo "========================================"
echo "Forward localhost:80 to Traefik"
echo "========================================"
echo ""

#forward port localhost:80 to traefik   
kubectl port-forward svc/traefik 80:80 -n traefik


