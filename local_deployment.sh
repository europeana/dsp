#!/bin/bash

set -e

# ============================================================
# Configuration
# ============================================================

DOCKER_HUB_USERNAME="${1:?Docker Hub username is required}"
IMAGE_TAG="${2:-develop}"

K8S_DIR="k8s/dev"
TMP_DIR=$(mktemp -d)

echo ""
echo "========================================"
echo "Deploying EDC DEV environment"
echo "========================================"

echo "Docker Hub username : ${DOCKER_HUB_USERNAME}"
echo "Docker image tag    : ${IMAGE_TAG}"
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

if [ ! -f "${K8S_DIR}/kustomization.yaml" ]; then
    echo "ERROR: ${K8S_DIR}/kustomization.yaml not found"
    exit 1
fi

# ============================================================
# Copy only DEV Kubernetes configuration
# ============================================================

echo "Preparing DEV Kubernetes configuration..."

cp -R "${K8S_DIR}/." "${TMP_DIR}/"

# ===============================================================================================================================================
# Set Docker images dynamically. image: europeana_controlplane:latest will become DOCKER_HUB_USERNAME/europeana_controlplane:IMAGE_TAG
# this is only changed at runtime and we can run with any branch we want. make sure images are deployed via jenkins job
# =============================================================================================================================================

cd "${TMP_DIR}"

kustomize edit set image \
    europeana_controlplane="${DOCKER_HUB_USERNAME}/europeana_controlplane:${IMAGE_TAG}"

kustomize edit set image \
    europeana_issuerservice="${DOCKER_HUB_USERNAME}/europeana_issuerservice:${IMAGE_TAG}"

kustomize edit set image \
    europeana_identity-hub="${DOCKER_HUB_USERNAME}/europeana_identity-hub:${IMAGE_TAG}"

kustomize edit set image \
    europeana_dataplane="${DOCKER_HUB_USERNAME}/europeana_dataplane:${IMAGE_TAG}"

# ============================================================
# Show images that will be deployed
# ============================================================

echo ""
echo "Images to deploy:"
echo ""

kustomize build .

# ============================================================
# Deploy to Kubernetes
# ============================================================

echo ""
echo "Applying Kubernetes configuration..."

kubectl apply -k .

# ============================================================
# Status
# ============================================================

echo ""
echo "========================================"
echo "EDC DEV DEPLOYMENT COMPLETED"
echo "========================================"

echo ""
echo "Docker Hub username : ${DOCKER_HUB_USERNAME}"
echo "Image tag           : ${IMAGE_TAG}"
echo ""

kubectl get pods --all-namespaces