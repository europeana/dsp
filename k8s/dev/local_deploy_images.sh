#!/bin/bash

set -euo pipefail

# ============================================================
# Configuration
# ============================================================

BRANCH="${BRANCH:-$(git rev-parse --abbrev-ref HEAD)}"
BRANCH="${BRANCH#*/}"

echo "========================================"
echo "EDC Local Build"
echo "Branch : ${BRANCH}"
echo "========================================"

# ============================================================
# Buildx
# ============================================================

docker buildx create \
    --name edc-builder \
    --driver docker-container \
    --use \
    2>/dev/null || true

docker buildx inspect --bootstrap

# ============================================================
# Build Gradle artifacts
# ============================================================

echo "Building Gradle artifacts..."

./gradlew \
    :launchers:controlplane:shadowJar \
    :launchers:issuerservice:shadowJar \
    :launchers:identity-hub:shadowJar \
    :launchers:dataplane:shadowJar

# ============================================================
# Function to build a local Docker image
# ============================================================

build_local_image() {

    IMAGE_NAME="europeana/dsp-$1"
    CONTEXT="$2"
    DOCKERFILE="$3"
    JAR="$4"

    echo ""
    echo "========================================"
    echo "Building ${IMAGE_NAME}:${BRANCH}"
    echo "========================================"

    docker buildx build \
        --platform linux/amd64 \
        --file "${DOCKERFILE}" \
        --build-arg JAR="${JAR}" \
        --tag "${IMAGE_NAME}:${BRANCH}" \
        --load \
        "${CONTEXT}"

    echo "Built ${IMAGE_NAME}:${BRANCH}"
}

# ============================================================
# Build all EDC images locally
# ============================================================

build_local_image \
    "controlplane" \
    "launchers/controlplane" \
    "launchers/controlplane/src/main/docker/Dockerfile" \
    "build/libs/controlplane.jar"

build_local_image \
    "issuerservice" \
    "launchers/issuerservice" \
    "launchers/issuerservice/src/main/docker/Dockerfile" \
    "build/libs/issuerservice.jar"

build_local_image \
    "identity-hub" \
    "launchers/identity-hub" \
    "launchers/identity-hub/src/main/docker/Dockerfile" \
    "build/libs/identity-hub.jar"

build_local_image \
    "dataplane" \
    "launchers/dataplane" \
    "launchers/dataplane/src/main/docker/Dockerfile" \
    "build/libs/dataplane.jar"

echo ""
echo "========================================"
echo "ALL LOCAL IMAGES BUILT SUCCESSFULLY"
echo "========================================"

docker images | grep europeana/dsp