#!/bin/bash

if [ -z "$MODE" ]; then
    export MODE="file2Vault"
fi

if [ -z "$CERTIFICATES_FOLDER" ]; then
    export CERTIFICATES_FOLDER="/etc/letsencrypt/live"
fi

if [ -z "$CERTIFICATES_EXTENSION" ]; then
    export CERTIFICATES_EXTENSION="pem"
fi

if [ -z "$VAULT_ADDRESS" ]; then
    export VAULT_ADDRESS="http://172.17.0.1:1234"
fi

if [ -z "$VAULT_PATH" ]; then
    export VAULT_PATH="kv/certificates"
fi

if [ -z "$VAULT_TOKEN" ]; then
    export VAULT_TOKEN="myroot"
fi

if [ -z "$DYNAMIC_PATH_FOLDER" ]; then
    export DYNAMIC_PATH_FOLDER="/etc/letsencrypt/meta"
fi

exec java -jar sync-certificates-vault.jar "${MODE}" "${CERTIFICATES_FOLDER}" "${CERTIFICATES_EXTENSION}" "${VAULT_ADDRESS}" "${VAULT_TOKEN}" "${VAULT_PATH}" "$DYNAMIC_PATH_FOLDER"
