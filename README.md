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
- Order domain: REST API, status lifecycle, and status history audit trail

Webhook, webhook log, and mail notification domain implementations are intentionally not included yet.

## Order API

All endpoints require a JWT access token and operate on the orders of the authenticated customer.

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/orders` | Create an order; the total is calculated from the items |
| `GET` | `/api/orders` | Paginated list, filterable by `status`, `from`, `to` |
| `GET` | `/api/orders/{id}` | Single order with items and status history |
| `GET` | `/api/orders/number/{orderNumber}` | Look up an order by its business identifier |
| `GET` | `/api/orders/{id}/status-history` | Status audit trail of the order |
| `PUT` | `/api/orders/{id}` | Replace address and items while still `PENDING_PAYMENT` |
| `PATCH` | `/api/orders/{id}/cancel` | Cancel an order that has not been shipped |
| `DELETE` | `/api/orders/{id}` | Soft delete a cancelled or delivered order |

### Order lifecycle

```
PENDING_PAYMENT -> PAID | PAYMENT_FAILED | CANCELLED
PAID            -> SHIPPED | CANCELLED
PAYMENT_FAILED  -> PENDING_PAYMENT | CANCELLED
SHIPPED         -> DELIVERED
DELIVERED, CANCELLED -> final
```

Transitions are validated in `OrderStatus`, and every change is written to `order_status_history`
together with its source (`CUSTOMER`, `SYSTEM`, or the webhook that triggered it).

### Entry points for the webhook module

The webhook handlers do not touch orders directly; they call `OrderService`:

- `applyExternalStatusChange(orderNumber, targetStatus, reason, source)` — validates the transition,
  updates the order, appends the audit entry, and ignores duplicate events for the same status
- `applyStatusChange(order, targetStatus, reason, source)` — same, when the order is already loaded
- `attachPaymentReference(orderNumber, paymentReference)`
- `attachTrackingNumber(orderNumber, trackingNumber)`

`OrderRepository` additionally exposes `findByOrderNumber` and `findByPaymentReference` for
resolving the order an external event belongs to. Each of these methods returns the updated
`Order`, so the notification module can send the customer email from the returned state.

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
