# Notifications Plugin — Field-Encryption Key Rotation Runbook

**Audience:** OpenSearch cluster operators / site-reliability engineers  
**Last updated:** 2026-05-11  
**Estimated downtime:** Zero (rolling procedure)

---

## Background

Sensitive channel-configuration fields (e.g. webhook URLs, API tokens) are
encrypted at rest with AES-256-GCM. Every ciphertext carries a **format
version** sentinel — not a key version:

```
enc:v1:<Base64(nonce || ciphertext || GCM-tag)>
```

`v1` identifies the encryption format (AES-256-GCM, 12-byte random IV,
16-byte GCM auth tag). It is **not** tied to which key produced the
ciphertext and will only change if the algorithm or binary layout changes.

Key selection at decrypt time is purely positional:

1. Try the **active key** (`opensearch.notifications.field_encryption_key`).
2. If that fails, try the **previous key** (`opensearch.notifications.field_encryption_key_previous`) — present only during a rotation window.
3. If neither succeeds, raise a decryption error (logged with config ID, never values).

There is **no version counter** cluster setting. The operator never needs to
know or record "the current version number".

Two keystore settings drive the feature:

| Setting | Type | Description |
|---|---|---|
| `opensearch.notifications.field_encryption_key` | Secure (keystore) | Current AES-256 key — used for all new encryptions |
| `opensearch.notifications.field_encryption_key_previous` | Secure (keystore) | Previous AES-256 key — present only during the rotation window |

---

## Prerequisites

* `opensearch-keystore` is available on every data / coordinator node.
* You have shell access to every node (or an automation layer such as Ansible /
  Kubernetes secrets).
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
# Confirm encryption is enabled and the plugin is running normally
curl -s -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs \
  | python3 -m json.tool

# Spot-check that newly written configs carry the enc:v1: prefix
# (visible in the raw index document, not the decrypted API response)
curl -s -u admin:password \
  'https://localhost:9200/.opensearch-notifications-config/_search?size=1' \
  | python3 -m json.tool | grep enc
```

---

## Phase 1 — Provision the new key on every node

Perform the following steps **on each node, one at a time**, before reloading
settings. The cluster remains fully operational throughout.

```bash
# 1a. Promote the CURRENT active key to "previous" so existing
#     ciphertexts can still be decrypted during the rotation window
echo "<OLD_BASE64_KEY>" | \
  opensearch-keystore add -x -f opensearch.notifications.field_encryption_key_previous

# 1b. Set the NEW key as the active key
echo "<NEW_BASE64_KEY>" | \
  opensearch-keystore add -x -f opensearch.notifications.field_encryption_key
```

> **Tip — automated via Ansible:**
> ```yaml
> - name: Set previous encryption key (old active value)
>   opensearch_keystore:
>     name: opensearch.notifications.field_encryption_key_previous
>     value: "{{ old_field_encryption_key }}"
>
> - name: Set new active encryption key
>   opensearch_keystore:
>     name: opensearch.notifications.field_encryption_key
>     value: "{{ new_field_encryption_key }}"
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
- **Active key** (new value) → used for all new encryptions
- **Previous key** (old value) → used to decrypt existing ciphertexts only

No version counter needs to be bumped. New writes immediately use the new key.

---

## Phase 3 — Re-encrypt existing ciphertexts

Migrate all stored ciphertexts to the new key by calling the re-encryption
admin endpoint:

```bash
curl -s -X POST -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs/_reencrypt \
  | python3 -m json.tool
```

Monitor progress — the endpoint returns counts of migrated and failed records.
Re-run the call until `"remaining": 0`.

> **Note:** The endpoint is idempotent. Ciphertexts that can already be
> decrypted by the active key are skipped automatically.

**Verification:**

```bash
# Create + read back a test channel — a round-trip success means the
# active key is working correctly.
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

## Phase 4 — Remove the previous key from every node

Once `"remaining": 0`, the old key is no longer needed. Remove it from every
node's keystore:

```bash
opensearch-keystore remove opensearch.notifications.field_encryption_key_previous
```

Reload secure settings again to close the rotation window:

```bash
curl -s -X POST -u admin:password \
  https://localhost:9200/_nodes/reload_secure_settings \
  -H 'Content-Type: application/json' \
  -d '{}' \
  | python3 -m json.tool
```

---

## Phase 5 — Post-rotation verification

```bash
# 1. Round-trip test — create, read, delete a channel
CHANNEL_ID=$(curl -s -X POST -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs \
  -H 'Content-Type: application/json' \
  -d '{
    "config": {
      "name": "post-rotation-test",
      "config_type": "webhook",
      "webhook": { "url": "https://example.com/post-rotation-probe" }
    }
  }' | python3 -m json.tool | grep '"config_id"' | awk -F'"' '{print $4}')

curl -s -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs/${CHANNEL_ID} \
  | python3 -m json.tool

curl -s -X DELETE -u admin:password \
  https://localhost:9200/_plugins/_notifications/configs/${CHANNEL_ID}

# 2. Confirm there are no decryption errors in the plugin log
grep -i "decryption failed" /path/to/opensearch/logs/opensearch.log | tail -20
```

---

## Rollback procedure

**Before Phase 3** (re-encrypt not yet run): no existing ciphertext has been
re-encrypted, so rollback is safe:

1. On every node, restore the old key to `field_encryption_key` and remove
   `field_encryption_key_previous`.
2. Call `POST /_nodes/reload_secure_settings`.

**After Phase 3** (re-encryption complete or in progress): both keys are still
registered. Do **not** remove `field_encryption_key_previous` until
re-encryption is confirmed complete. To roll back, swap the keys (restore old
as `field_encryption_key`, set new as `field_encryption_key_previous`), reload
settings, and re-run `_reencrypt` to bring all ciphertexts back to the old key.

---

## Checklist

| Step | Command / Action | Done |
|------|-----------------|------|
| 0 | Verify current state; generate new key | ☐ |
| 1 | Provision new active key + old previous key on **all** nodes | ☐ |
| 2 | `POST /_nodes/reload_secure_settings` | ☐ |
| 3 | `POST /_plugins/_notifications/configs/_reencrypt` until `remaining == 0` | ☐ |
| 4 | Remove `field_encryption_key_previous` from all nodes + reload | ☐ |
| 5 | Post-rotation verification | ☐ |

---

## Troubleshooting

| Symptom | Likely cause | Remedy |
|---------|-------------|--------|
| `DecryptionException: Decryption failed` in logs | Active key does not match the key that produced the ciphertext, and no `_previous` key is registered | Check that `field_encryption_key` holds the correct value; if mid-rotation, ensure `field_encryption_key_previous` is also set and reload |
| `AEADBadTagException` on read | Keystore entry was corrupted or wrong key bytes were stored | Restore the correct Base64 key bytes and reload |
| `_reencrypt` endpoint returns failures | Some records could not be decrypted by either key | Check plugin logs for the offending config ID; register the matching key as `field_encryption_key_previous` and retry |
| Node reloaded but still failing | Keystore entries were set on some nodes but not all | Re-verify all nodes; re-run `_nodes/reload_secure_settings` after all nodes are updated |
