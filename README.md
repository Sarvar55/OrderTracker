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

## Webhook API

External payment and shipment providers notify OrderTracker of status changes by posting signed
events to these endpoints. Requests are **not** authenticated with a JWT — instead, each request
must carry a valid HMAC-SHA256 signature computed over the raw request body.

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/webhooks/payment` | Payment gateway status update (`payment.succeeded`, `payment.failed`) |
| `POST` | `/api/webhooks/shipment` | Shipping provider status update (`shipment.shipped`, `shipment.delivered`) |

### Signature verification

Every request must include an `X-Webhook-Signature` header in the form:

```
X-Webhook-Signature: sha256=<hex-encoded HMAC-SHA256 of the raw body>
```

The HMAC key is `WEBHOOK_SECRET`, the same value configured in Vault (see
[Start infrastructure](#start-infrastructure)). Requests with a missing, malformed, or
mismatched signature are rejected before the payload is parsed.

To compute a valid signature locally:

```bash
BODY='{"eventId":"evt_001","eventType":"payment.succeeded","orderNumber":"ORD-20260827-4F2A9C31","paymentReference":"pi_3QkL2mF9x","amount":358.80}'
SIG=$(echo -n "$BODY" | openssl dgst -sha256 -hmac "$WEBHOOK_SECRET" | sed 's/^.* //')

curl -X POST http://localhost:8082/api/webhooks/payment \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Signature: sha256=$SIG" \
  -d "$BODY"
```

### Payment webhook payload

```json
{
  "eventId": "evt_1PxK2mF9x0002",
  "eventType": "payment.succeeded",
  "orderNumber": "ORD-20260827-4F2A9C31",
  "paymentReference": "pi_3QkL2mF9x",
  "amount": 358.80,
  "failureReason": null
}
```

`eventType` must be `payment.succeeded` or `payment.failed`. `failureReason` is only relevant
for failed payments.

### Shipment webhook payload

```json
{
  "eventId": "evt_2QyM3nG0y0003",
  "eventType": "shipment.shipped",
  "orderNumber": "ORD-20260827-4F2A9C31",
  "trackingNumber": "1Z999AA10123456784"
}
```

`eventType` must be `shipment.shipped` or `shipment.delivered`.

### Idempotency and logging

Every incoming request is persisted as a `WebhookEvent` (payload, signature validity, resulting
status, timestamp) before it is processed. Events are de-duplicated by `eventId` per channel, so
a provider retrying the same event will not double-apply an order status change.

### Webhook log API (admin)

Every received webhook event is queryable through an admin-only audit API, backed by the same
`WebhookEvent` records described above.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/webhooks/logs` | Paginated, filterable list of webhook events |
| `GET` | `/api/webhooks/logs/{id}` | Full detail of a single event, including the raw payload |

Both endpoints require a JWT with the `ADMIN` role.

#### List filters (`GET /api/webhooks/logs`)

| Param | Type | Description |
| --- | --- | --- |
| `channel` | `PAYMENT` \| `SHIPMENT` | Restrict to one webhook source |
| `status` | `RECEIVED` \| `PROCESSED` \| `FAILED` \| `IGNORED` | Restrict to one processing outcome |
| `from` | ISO-8601 date-time | Only events received at or after this timestamp |
| `to` | ISO-8601 date-time | Only events received at or before this timestamp |
| `page`, `size`, `sort` | standard Spring `Pageable` | Defaults to `size=20`, sorted by `createdAt` descending |

```bash
curl -H "Authorization: Bearer $ADMIN_JWT" \
  "http://localhost:8082/api/webhooks/logs?channel=PAYMENT&status=FAILED&from=2026-08-01T00:00:00"
```

Response entries include `channel`, `eventType`, `providerEventId`, `orderNumber`,
`signatureValid`, `status`, `errorMessage`, `receivedAt`, and `processedAt` — but not the raw
payload, to keep list responses compact.

#### Event detail (`GET /api/webhooks/logs/{id}`)

```bash
curl -H "Authorization: Bearer $ADMIN_JWT" \
  http://localhost:8082/api/webhooks/logs/42
```

Returns everything in the list view plus the full `payload` as received, for troubleshooting a
specific event.

## Additional Features

### Email notification retry

Order-status-change emails are sent asynchronously and automatically retried on transient
failures (e.g. SMTP timeouts), using Spring Retry with exponential backoff.

- Up to **3 attempts**, starting at a 2s delay and doubling each retry (2s → 4s → 8s)
- Retries only trigger on `MailException` (transient send failures), not on message-construction
  errors
- If all attempts fail, the failure is logged (not silently dropped) rather than retried
  indefinitely — no separate dead-letter queue is used

No new endpoint is exposed for this; it's an internal reliability improvement to the existing
async mail flow triggered by order status changes (including webhook-driven ones).

### Order history export (CSV)

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/orders/export` | Export the authenticated customer's own orders as a CSV file |

Requires a JWT (same auth as the rest of the Order API). Filterable and downloads as an
`attachment; filename="orders.csv"`.

| Param | Type | Description |
| --- | --- | --- |
| `status` | `OrderStatus` | Optional — restrict to one order status |
| `from` | ISO-8601 date-time | Optional — only orders created at or after this timestamp |
| `to` | ISO-8601 date-time | Optional — only orders created at or before this timestamp |

```bash
curl -H "Authorization: Bearer $JWT" \
  "http://localhost:8082/api/orders/export?status=DELIVERED&from=2026-08-01T00:00:00" \
  -o orders.csv
```

Exported columns: `Order Number, Status, Total Amount, Currency, Items, Payment Reference,
Tracking Number, Created At, Updated At`.

> **Note:** Only CSV export is currently implemented. Excel (`.xlsx`) export is not yet available.

### Admin dashboard statistics

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/admin/dashboard/stats` | Aggregated order and webhook statistics |

Requires a JWT with the `ADMIN` role.

```bash
curl -H "Authorization: Bearer $ADMIN_JWT" http://localhost:8082/api/admin/dashboard/stats
```

Response shape:

```json
{
  "totalOrders": 128,
  "ordersByStatus": { "PENDING_PAYMENT": 12, "PAID": 40, "SHIPPED": 30, "DELIVERED": 46 },
  "totalWebhookEvents": 96,
  "webhookEventsByStatus": { "PROCESSED": 90, "FAILED": 6 },
  "webhookSuccessRate": 0.9375
}
```

`webhookSuccessRate` is `PROCESSED` events divided by total webhook events received.

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
