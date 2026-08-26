# Vault UI setup

Open `http://localhost:8200`, select the **Token** authentication method, and sign in with the development token `root`.

## 1. Enable AppRole

Open **Access → Authentication Methods → Enable new method**, select **AppRole**, and use the path `approle`.

## 2. Create the policy

Open **Policies → ACL Policies → Create ACL policy**.

- Name: `order-tracker`
- Policy: paste the complete contents of `vault/order-tracker-policy.hcl`

## 3. Create the KV secret

Open **Secrets Engines → secret → Create secret**.

- Path: `order-tracker-service/dev`
- Secret data: copy all keys and values from your local `vault/order-tracker-secrets.json`

Required keys:

- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `WEBHOOK_SECRET`

## 4. Create the AppRole

If the UI does not expose AppRole role creation, run the files under `vault/commands` in numeric order from a terminal where `VAULT_ADDR=http://localhost:8200` and `VAULT_TOKEN=root` are set.

Put the result of command `04` into `VAULT_ROLE_ID` in `.env`. Put the result of command `05` into `VAULT_SECRET_ID` in `.env`.

Never commit `.env` or `vault/order-tracker-secrets.json`.
