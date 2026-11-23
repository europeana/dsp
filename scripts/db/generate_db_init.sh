#!/usr/bin/env bash
set -euo pipefail

export ENVIRONMENT=

#KC DB
export KC_DB_USER=
export KC_DB_USER_PASS=

#ISSUER DB
export ISSUER_DB_USER=
export ISSUER_DB_USER_PASS=

#CONSUMER DB
export CONSUMER_DB_CP_USER=
export CONSUMER_DB_CP_USER_PASS=
export CONSUMER_DB_DP_USER=
export CONSUMER_DB_DP_USER_PASS=
export CONSUMER_DB_IH_USER=
export CONSUMER_DB_IH_USER_PASS=

#PROVIDER DB
export PROVIDER_DB_CP_USER=
export PROVIDER_DB_CP_USER_PASS=
export PROVIDER_DB_DP_USER=
export PROVIDER_DB_DP_USER_PASS=
export PROVIDER_DB_IH_USER=
export PROVIDER_DB_IH_USER_PASS=

#create environment folder
mkdir -p ./init-${ENVIRONMENT}


TEMPLATE_FILE="config.template.yaml"
OUTPUT_FILE="config.yaml"

# 2. Replace ONLY the specified variables and write to the output file
envsubst '${ENVIRONMENT} ${KC_DB_USER} ${KC_DB_USER_PASS}' < "./init/kc-db-init.sql.template" > "./init-${ENVIRONMENT}/kc-db-init.sql"
envsubst '${ENVIRONMENT} ${ISSUER_DB_USER} ${KC_DB_USER_PASS}' < "./init/issuer-db-init.sql.template" > "./init-${ENVIRONMENT}/issuer-db-init.sql"

envsubst '${ENVIRONMENT} ${CONSUMER_DB_CP_USER} ${CONSUMER_DB_CP_USER_PASS} ${CONSUMER_DB_DP_USER} ${CONSUMER_DB_DP_USER_PASS}' < "./init/consumer-db-init.sql.template" > "./init-${ENVIRONMENT}/consumer-db-init.sql"
envsubst '${ENVIRONMENT} ${CONSUMER_DB_IH_USER} ${CONSUMER_DB_IH_USER_PASS} ' < "./init/consumer-ih-db-init.sql.template" > "./init-${ENVIRONMENT}/consumer-ih-db-init.sql"

envsubst '${ENVIRONMENT} ${PROVIDER_DB_CP_USER} ${PROVIDER_DB_CP_USER_PASS} ${PROVIDER_DB_DP_USER} ${PROVIDER_DB_DP_USER_PASS}' < "./init/provider-db-init.sql.template" > "./init-${ENVIRONMENT}/provider-db-init.sql"
envsubst '${ENVIRONMENT} ${PROVIDER_DB_IH_USER} ${PROVIDER_DB_IH_USER_PASS} ' < "./init/provider-ih-db-init.sql.template" > "./init-${ENVIRONMENT}/provider-ih-db-init.sql"



echo "Generated init-${ENVIRONMENT} successfully."

 
