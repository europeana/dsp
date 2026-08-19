#!/bin/bash

set -e

# ============================================================
# Configuration
# ============================================================

DOCKER_HUB_ORAGANISATION="${1:?Docker Hub organisation is required}"
IMAGE_TAG="${2:-develop}"

K8S_DIR="k8s/dev"
TMP_DIR=$(mktemp -d)

echo ""
echo "========================================"
echo "Deploying EDC DEV environment"
echo "========================================"

echo "Docker Hub Organisation : ${DOCKER_HUB_ORAGANISATION}"
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

if [ ! -f "${K8S_DIR}/kustomization.yml" ]; then
    echo "ERROR: ${K8S_DIR}/kustomization.yml not found"
    exit 1
fi

# ============================================================
# Copy only DEV Kubernetes configuration
# ============================================================

echo "Preparing DEV Kubernetes configuration..."

cp -R "${K8S_DIR}/." "${TMP_DIR}/"

# ===============================================================================================================================================
# Set Docker images dynamically. image: dsp-controlplane:latest will become DOCKER_HUB_ORAGANISATION/dsp-controlplane:IMAGE_TAG
# this is only changed at runtime and we can run with any branch we want. make sure images are deployed via jenkins job
# =============================================================================================================================================

cd "${TMP_DIR}"

kustomize edit set image \
    dsp-controlplane="${DOCKER_HUB_ORAGANISATION}/dsp-controlplane:${IMAGE_TAG}"

kustomize edit set image \
    dsp-issuerservice="${DOCKER_HUB_ORAGANISATION}/dsp-issuerservice:${IMAGE_TAG}"

kustomize edit set image \
    dsp-identity-hub="${DOCKER_HUB_ORAGANISATION}/dsp-identity-hub:${IMAGE_TAG}"

kustomize edit set image \
    dsp-dataplane="${DOCKER_HUB_ORAGANISATION}/dsp-dataplane:${IMAGE_TAG}"

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
echo "Docker Hub organisation : ${DOCKER_HUB_ORAGANISATION}"
echo "Image tag           : ${IMAGE_TAG}"
echo ""

kubectl get pods --all-namespaces