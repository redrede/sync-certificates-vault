# Sync certificates to HashiCorp Vault

The synchronization java application must be running and when certificates are created or changed, it will automatically send them to the Vault.

java -jar sync-certificates-vault.jar certificates-path certificates-extension vault-address vault-token  vault-path [dynamic-path]

dynamic-path: defines the folder, where a file with the domain name contains a part of the used dynamic path on the vault path.

Ex.:

java -jar sync-certificates-vault.jar "/etc/letsencrypt/live" "pem" "http://127.0.0.1:1234" "myroot" "kv/certificates"

Note: Files created or changed prior to running the app will not be uploaded.

## Added support for Approle authentication

java -jar sync-certificates-vault.jar certificates-path certificates-extension vault-address approle:&lt;role-id&gt;:&lt;secret-id&gt; vault-path [dynamic-path]

Ex.:

java -jar sync-certificates-vault.jar "/etc/letsencrypt/live" "pem" "http://127.0.0.1:1234" "approle:fae47a5f-17b1-7ffd-646b-16371515a878:72e44acb-b987-52f7-735e-b01dd6e91ab9" "kv/certificates"


# Test:

docker run --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:1234' -p 1234:1234 vault

# Docker:

## Build:

docker build -t redrede/sync-certificates-vault .

## Env:

CERTIFICATES_FOLDER - default value: /etc/letsencrypt/live

CERTIFICATES_EXTENSION - default value: pem

VAULT_ADDRESS - default value: http://172.17.0.1:1234

VAULT_PATH - default value: kv/certificates

VAULT_TOKEN - default value: myroot

DYNAMIC_PATH_FOLDER - default value: /etc/letsencrypt/meta"

## Run:

docker run -d -v /etc/letsencrypt/live:/etc/letsencrypt/live -v /etc/letsencrypt/meta:/etc/letsencrypt/meta ghcr.io/redrede/sync-certificates-vault:main


