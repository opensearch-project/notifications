# Notifications Plugin — Field-Encryption Key Rotation Runbook

**Audience:** OpenSearch cluster operators / site-reliability engineers  
**Last updated:** 2026-04-30  
**Estimated downtime:** Zero (rolling procedure)

---

## Background

Sensitive channel-configuration fields (e.g. webhook URLs, API tokens) are
encrypted at rest with AES-256-GCM. Every ciphertext is tagged with the key
version that produced it:

```
enc:v<N>:<Base64(nonce || ciphertext || GCM-tag)>
```

Three keystore / cluster settings drive the feature:

| Setting | Type | Description |
|---|---|---|
| `opensearch.notifications.field_encryption_key` | Secure (keystore) | Current AES-256 key — used for all new encryptions |
| `opensearch.notifications.field_encryption_key_previous` | Secure (keystore) | Previous AES-256 key — kept during the rotation window so old ciphertexts can still be decrypted |
| `opensearch.notifications.field_encryption_key_version` | `integer` (cluster setting) | Version number stamped on newly-written ciphertexts. Increment by 1 on every rotation |

---

## Prerequisites

* `opensearch-keystore` is available on every data / coordinator node.
* You have shell access to every node (or an automation layer such as Ansible /
  Kubernetes secrets).
* You know the current key version **N** (visible via
  `GET _cluster/settings?include_defaults=true` → look for
  `opensearch.notifications.field_encryption_key_version`; default is `1`).
* You have generated a new 256-bit AES key:

  ```bash
  # Generate a Base64-encoded 32-byte random key
  openssl rand -base64 32
  # Example output: 4Gp8Kz3mXqR7vN2wLjY5hU0dCsO1eIbA9fTnPuQlWxM=
  ```

  Store it securely (e.g. in a secrets manager) before continuing.

---

## Phase 0 — Verify the current state

```bash
# Check current version (should equal N)
curl -s -u admin:password https://localhost:9200/_cluster/settings?include_defaults=true \
  | python3 -m json.tool \
  | grep field_encryption_key_version

# Confirm the plugin is running in encryption mode on at least one node:
# Any notification-channel read/write in the audit log should show enc:vN: prefixes
# on sensitive fields.
```

---

## Phase 1 — Provision the new key on every node

Perform the following steps **on each node, one at a time**, before reloading
settings. The cluster remains fully operational throughout.

```bash
# 1a. Write the NEW key as the current key
echo "<NEW_BASE64_KEY>" | \
  opensearch-keystore add -x -f opensearch.notifications.field_encryption_key

# 1b. Write the OLD (current) key as the previous key so existing
#     ciphertexts tagged enc:vN: can still be decrypted during the window
echo "<OLD_BASE64_KEY>" | \
  opensearch-keystore add -x -f opensearch.notifications.field_encryption_key_previous
```

> **Tip — automated via Ansible:**
> ```yaml
> - name: Set new encryption key
>   opensearch_keystore:
>     name: opensearch.notifications.field_encryption_key
>     value: "{{ new_field_encryption_key }}"
>
> - name: Set previous encryption key
>   opensearch_keystore:
>     name: opensearch.notifications.field_encryption_key_previous
>     value: "{{ old_field_encryption_key }}"
> ```

Repeat for every node in the cluster before moving to Phase 2.

---

## Phase 2 — Reload secure settings cluster-wide

Once **all** nodes have both keystore entries, reload the settings without
restarting the cluster:

```bash
curl -s -X POST -u admin:password \
  https://localhost:9200/_nodes/reload_secure_settings \
  -H 'Content-Type: application/json' \
  -d '{}' \
  | python3 -m json.tool
```

**Expected response:** every node reports `"successful": true` with no failures.

At this point every node has loaded:
- New key → used for encryption (still writing `enc:v**N**:` until Phase 3)
- Old key → available to decrypt existing `enc:v**N**:` values

---

## Phase 3 — Increment the version number

Bump the version counter to **N+1** so that all new writes use the new key:

```bash
curl -s -X PUT -u admin:password \
  https://localhost:9200/_cluster/settings \
  -H 'Content-Type: application/json' \
  -d '{
    "persistent": {
      "opensearch.notifications.field_encryption_key_version": <N+1>
    }
  }' \
  | python3 -m json.tool
```

**Verification:** create or update a notification channel and confirm the
stored ciphertext now carries the `enc:v<N+1>:` prefix.

From this moment forward, all **new** writes use `enc:v<N+1>:` (new key) while
all **reads** can still decrypt `enc:v<N>:` values (old key, still registered
as `field_encryption_key_previous`).

---

## Phase 4 — Re-encrypt existing ciphertexts (migration)

Migrate all stored ciphertexts from version N to N+1 by calling the
re-encryption admin endpoint:

```bash
curl -s -X POST -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs/_reencrypt \
  | python3 -m json.tool
```

Monitor progress — the endpoint returns counts of migrated and failed records.
Re-run the call until `"remaining": 0`.

> **Note:** The endpoint is idempotent. Already-migrated `enc:v<N+1>:` values
> are skipped automatically.

**Verification:**

```bash
# No enc:vN: values should remain in any channel config
curl -s -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs \
  | grep -c "enc:v${N}:"
# Expected output: 0
```

---

## Phase 5 — Remove the previous key from every node

Once `"remaining": 0`, the old key is no longer needed. Remove it from every
node's keystore:

```bash
opensearch-keystore remove opensearch.notifications.field_encryption_key_previous
```

Reload secure settings again to make the removal effective:

```bash
curl -s -X POST -u admin:password \
  https://localhost:9200/_nodes/reload_secure_settings \
  -H 'Content-Type: application/json' \
  -d '{}' \
  | python3 -m json.tool
```

---

## Phase 6 — Post-rotation verification

```bash
# 1. Confirm version is N+1
curl -s -u admin:password \
  https://localhost:9200/_cluster/settings?include_defaults=true \
  | python3 -m json.tool | grep field_encryption_key_version

# 2. Confirm no old-version ciphertexts remain
curl -s -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs \
  | grep -c "enc:v${N}:"
# Expected: 0

# 3. Round-trip test — create a channel, read it back, delete it
CHANNEL_ID=$(curl -s -X POST -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs \
  -H 'Content-Type: application/json' \
  -d '{
    "config": {
      "name": "rotation-test",
      "config_type": "webhook",
      "webhook": { "url": "https://example.com/rotation-probe" }
    }
  }' | python3 -m json.tool | grep '"config_id"' | awk -F'"' '{print $4}')

curl -s -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs/${CHANNEL_ID} \
  | python3 -m json.tool

curl -s -X DELETE -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs/${CHANNEL_ID}
```

---

## Rollback procedure

If any phase fails **before Phase 3** (version bump), the cluster is still
operating on version N and no data has been re-encrypted. Rollback is safe:

1. Remove `opensearch.notifications.field_encryption_key_previous` from every
   node's keystore.
2. Restore the old key to `opensearch.notifications.field_encryption_key`.
3. Call `POST /_nodes/reload_secure_settings`.

If the failure occurs **after Phase 3**, the rotation window is active (both
keys are registered). Do **not** remove `field_encryption_key_previous` until
re-encryption is complete or reversed.

---

## Checklist

| Step | Command / Action | Done |
|------|-----------------|------|
| 0 | Verify current version N and generate new key | ☐ |
| 1 | Provision new + old key on **all** nodes | ☐ |
| 2 | `POST /_nodes/reload_secure_settings` | ☐ |
| 3 | Bump `field_encryption_key_version` to N+1 | ☐ |
| 4 | `POST /_plugins/_notifications/configs/_reencrypt` until `remaining == 0` | ☐ |
| 5 | Remove `field_encryption_key_previous` from all nodes + reload | ☐ |
| 6 | Post-rotation verification | ☐ |

---

## Troubleshooting

| Symptom | Likely cause | Remedy |
|---------|-------------|--------|
| `IllegalArgumentException: no key registered for version N` in logs | Node reloaded before both keystore entries were present | Re-add `field_encryption_key_previous` on the affected node and reload |
| `AEADBadTagException` on read | Keystore entry was corrupted or wrong key was stored | Restore the correct Base64 key bytes and reload |
| `_reencrypt` endpoint returns failures | Some records have an unknown version tag | Check plugin logs for the offending version number; register the matching key |
| Version counter did not advance | Cluster-settings update was rejected | Check for validation errors; version must be ≥ 1 and an integer |

