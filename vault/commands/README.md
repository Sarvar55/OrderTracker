# Command order

Before running the command files:

```bash
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root
```

Run `01` through `06` in numeric order. If AppRole is already enabled, command `01` can return an already-in-use error and may be skipped.

If the application returns `403 permission denied`, run command `02` again. Creating an AppRole with `token_policies=order-tracker` does not create the policy itself; the `order-tracker` policy must also exist in Vault.
