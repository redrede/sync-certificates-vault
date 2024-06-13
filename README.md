# Sincronizar Certificados com HashiCorp Vault

Esta aplicação Java de sincronização deve estar em execução para garantir que, sempre que certificados sejam criados ou alterados, eles sejam automaticamente enviados para o Vault.

```
java -jar sync-certificates-vault.jar "<modo>" "<caminho-certificados>" "<extensao-certificados>" "<endereco-vault>" "<token-vault>" "<caminho-vault>" ["<caminho-dinamico>"]
```

caminho-dinamico:  define a pasta onde um arquivo com o nome de domínio contém uma parte do caminho dinâmico utilizado no caminho do Vault.

modo:  define se a sincronização será do certificado para o Vault (file2Vault) ou do Vault para o certificado

Exemplo:

```
java -jar sync-certificates-vault.jar "file2Vault" "/etc/letsencrypt/live" "pem" "http://127.0.0.1:1234" "myroot" "kv/certificates"
```

Nota: Arquivos criados ou alterados antes da execução do aplicativo não serão enviados.

## Suporte Adicionado para Autenticação AppRole

```
java -jar sync-certificates-vault.jar "<modo>" "<caminho-certificados>" "<extensao-certificados>" "<endereco-vault>" "approle:<role-id>:<secret-id>" "<caminho-vault>" ["<caminho-dinamico>"]
```

Exemplo:

```
java -jar sync-certificates-vault.jar "file2Vault" "/etc/letsencrypt/live" "pem" "http://127.0.0.1:1234" "approle:fae47a5f-17b1-7ffd-646b-16371515a878:72e44acb-b987-52f7-735e-b01dd6e91ab9" "kv/certificates"
```

# Teste:

```
docker run --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:1234' -p 1234:1234 vault
```

# Docker:

## Build:

```
docker build -t redrede/sync-certificates-vault .
```

## Variáveis de Ambiente:

MODE: valor padrão: file2Vault
CERTIFICATES_FOLDER: valor padrão: /etc/letsencrypt/live
CERTIFICATES_EXTENSION: valor padrão: pem
VAULT_ADDRESS: valor padrão: http://172.17.0.1:1234
VAULT_PATH: valor padrão: kv/certificates
VAULT_TOKEN: valor padrão: myroot
DYNAMIC_PATH_FOLDER: valor padrão: /etc/letsencrypt/meta

## Execução:

```
docker run -d -v /etc/letsencrypt/live:/etc/letsencrypt/live -v /etc/letsencrypt/meta:/etc/letsencrypt/meta ghcr.io/redrede/sync-certificates-vault:main
```
