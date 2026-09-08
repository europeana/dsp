#!/bin/bash
#ENSURE Linux EOL
set -e

TRAEFIK_VERSION=v3.7.5
# traefik chart version v41.0.0 repo:https://github.com/traefik/traefik-helm-chart/releases
#Traefik Proxy: v3.6.0 -> v3.7.5 (default)
#Traefik Hub: v3.19.3 -> v3.20.4
TRAEFIK_HELM_CHAR_VERSION=v41.0.0
#api gateway: v1.5.1  Repo: https://github.com/kubernetes-sigs/gateway-api/releases/
#getting started guidelines: https://github.com/kubernetes-sigs/gateway-api/blob/main/site/content/en/guides/getting-started/introduction.md
K8S_GATEWAY_API_VERSION=v1.5.1 


#kubectl apply --server-side --force-conflicts -f https://github.com/kubernetes-sigs/gateway-api/releases/download/${K8S_GATEWAY_API_VERSION}/experimental-install.yaml

# Step 2: Install Gateway API CRDs FIRST (before Traefik)
#compatible with Traefik 3.7 
kubectl apply --server-side --force-conflicts -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.5.1/experimental-install.yaml

# Step 3: Wait for CRDs to be established
kubectl wait --for=condition=Established crd/gateways.gateway.networking.k8s.io --timeout=60s
kubectl wait --for=condition=Established crd/httproutes.gateway.networking.k8s.io --timeout=60s

helm repo add traefik https://traefik.github.io/charts
helm repo update

#IMG TAG
helm upgrade --install \
    --namespace traefik \
    --create-namespace \
    --set image.tag="v3.7.5" \
    -f ./values-local.yaml \
    traefik traefik/traefik
    
kubectl rollout status deployment/traefik -n traefik --timeout=900s