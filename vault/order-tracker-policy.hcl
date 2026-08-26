path "sys/internal/ui/mounts/secret" {
  capabilities = ["read"]
}

path "sys/internal/ui/mounts/secret/*" {
  capabilities = ["read"]
}

path "secret/data/order-tracker-service" {
  capabilities = ["read"]
}

path "secret/data/order-tracker-service/*" {
  capabilities = ["read"]
}

path "secret/metadata/order-tracker-service" {
  capabilities = ["read"]
}

path "secret/metadata/order-tracker-service/*" {
  capabilities = ["read", "list"]
}
