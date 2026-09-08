#!/bin/bash
#ENSURE Linux EOL
set -e

    echo 'Removing DSP resources from Kubernetes cluster'
    REMOVE_DSP_COMMON=true
    if $REMOVE_DSP_COMMON; then 
      kubectl delete -n mvd-common deployment keycloak
      kubectl delete -n mvd-common configmap postgres-init keycloak-realm
      kubectl delete -n mvd-common deployment postgres
      kubectl delete httproutes --all -n mvd-common
    fi
    
    REMOVE_DSP_CONSUMER=true
    if $REMOVE_DSP_CONSUMER; then 
      kubectl delete -n consumer deployment vault postgres identityhub dataplane controlplane
        kubectl delete -n consumer configmap controlplane-config ih-config dataplane-config postgres-init
        kubectl delete -n consumer job identityhub-seed controlplane-seed vault-bootstrap
        kubectl delete -n consumer gateway consumer-gateway
        kubectl delete httproutes --all -n consumer
    fi
    
    REMOVE_DSP_PROVIDER=true
    if $REMOVE_DSP_PROVIDER; then 
      kubectl delete -n provider deployment vault postgres identityhub dataplane controlplane
        kubectl delete -n provider configmap controlplane-config ih-config dataplane-config postgres-init
        kubectl delete -n provider job identityhub-seed controlplane-seed vault-bootstrap
        kubectl delete -n provider gateway provider-gateway
        kubectl delete httproutes --all -n provider
    fi
    
    REMOVE_DSP_ISSUER=true
    if $REMOVE_DSP_ISSUER; then 
      kubectl delete -n issuer deployment vault postgres issuerservice
        kubectl delete -n issuer configmap issuerservice-config postgres-init
        kubectl delete -n issuer job issuerservice-seed vault-bootstrap
        kubectl delete httproutes --all -n issuer
    fi
