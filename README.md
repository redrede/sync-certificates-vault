# Sync certificates to Vault

The synchronization java application must be running and when certificates are created or changed, it will automatically send them to the Vault.

java -jar sync-certificates-vault.jar certificates-path certificates-extension vault-address vault-token  vault-path [dynamic-path]

dynamic-path: defines the folder, where a file with the domain name contains a part of the used dynamic path on the vault path.

Ex.:

java -jar sync-certificates-vault.jar "/etc/letsencrypt/live" "pem" "http://127.0.0.1:1234" "myroot" "kv/certificates"

Note: Files created or changed prior to running the app will not be uploaded.

# Test:

docker run --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:1234' -p 1234:1234 vault


