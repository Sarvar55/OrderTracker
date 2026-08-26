# OrderTracker

OrderTracker is a Spring Boot backend foundation for e-commerce order management and payment/shipment webhook integration. The current baseline intentionally contains only reusable common components, JWT authentication, PostgreSQL/Flyway, OpenAPI, Vault AppRole configuration, and Docker Compose infrastructure.

## Included now

- `POST /api/auth/register`
- `POST /api/auth/login`
- JWT authentication filter and stateless Spring Security
- Common response, exception, validation, audit, logging, CORS, and OpenAPI configuration
- PostgreSQL user schema managed by Flyway
- HashiCorp Vault KV v2 with AppRole authentication
- Docker Compose services for PostgreSQL, Vault dev server, and OrderTracker
- Spring Mail dependency and async foundation for the later notification module

Order, webhook, webhook log, and mail notification domain implementations are intentionally not included yet.

## Start infrastructure

Edit `.env`, then start PostgreSQL and Vault:

```bash
docker compose up -d db vault
```

Configure Vault using [vault/UI_SETUP.md](vault/UI_SETUP.md) or the numbered files under `vault/commands`.

The value of `DATABASE_PASSWORD` in `vault/order-tracker-secrets.json` must exactly match `DATABASE_PASSWORD` in `.env`, because Docker uses `.env` to initialize PostgreSQL while the application reads the same key from Vault.

After putting `VAULT_ROLE_ID` and `VAULT_SECRET_ID` into `.env`, start the application:

```bash
docker compose up --build app
```

Swagger UI: `http://localhost:8082/swagger-ui.html`

## Configuration consistency

Both `application-dev.yaml` and `application-prod.yaml` use exactly these datasource keys:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

Vault provides these application secrets without renaming:

- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `WEBHOOK_SECRET`
