# Plan: Encrypt Sensitive Notification Config Fields at Rest

Currently all channel configuration fields (Slack/Chime/Teams webhook URLs with embedded tokens, webhook `headerParams` with auth headers, etc.) are stored in plaintext in the `.opensearch-notifications-config` system index. This plan adds an **AES-256-GCM** encryption layer at the index read/write boundary, keyed by an entry in the OpenSearch node keystore — following the same `SecureSettings` pattern already used in `core/PluginSettings.kt` for SMTP credentials.

---

## Step 1 — Identify Sensitive Fields

| Channel Type | Field(s) to Encrypt | Reason |
|---|---|---|
| `Slack` | `url` | Embedded signing token |
| `Chime` | `url` | Embedded signing token |
| `MicrosoftTeams` | `url` | Embedded signing token |
| `Mattermost` | `url` | Embedded auth token |
| `Webhook` | `url`, all values in `headerParams` | API keys, `Authorization: Bearer`, `X-API-Key`, etc. |
| `SesAccount` / `Sns` | `roleArn`, `topicArn` _(optional tier)_ | Low-sensitivity ARNs; encrypt for compliance-heavy environments |

**Not requiring encryption:** `SmtpAccount` host/port/method/from (structural) and SMTP username/password — those are already stored exclusively in the OpenSearch keystore via the existing `EMAIL_USERNAME` / `EMAIL_PASSWORD` `SecureSetting` affix pattern.

---

## Step 2 — Create `FieldEncryptionService`

A new class in `notifications/util/FieldEncryptionService.kt`:

- **Constructor**: accepts a `SecretKey` derived from the keystore entry
- `encrypt(plaintext: String): String` — generates a 12-byte random IV, runs AES-256-GCM, Base64-encodes `IV || ciphertext || 16-byte auth tag`, returns `"enc:v1:<base64>"`
- `decrypt(value: String): String` — if the value **lacks** the `enc:v1:` prefix, returns it as-is (backward-compatible plaintext pass-through); otherwise decodes and decrypts
- `isEncrypted(value: String): Boolean` — utility for migration checks
- Zeroizes key material on `close()`; held as a singleton by `NotificationPlugin`

The `enc:v1:` sentinel prefix is the key design decision — it enables the transparent migration strategy without any schema changes. The `v1` encodes the **encryption format** (AES-256-GCM, 12-byte random IV, 16-byte GCM auth tag, Base64-encoded `IV || ciphertext || tag`). It is **not** a key version; it will only change if the algorithm or binary layout changes in the future.

---

## Step 3 — Register the Key via `SecureSettings`

**In `notifications/settings/PluginSettings.kt`:**
```kotlin
val FIELD_ENCRYPTION_KEY: Setting<SecureString> =
    SecureSetting.secureString("opensearch.notifications.field_encryption_key", null)
```

**In `NotificationPlugin.createComponents()`:**
1. Read the key: `FIELD_ENCRYPTION_KEY.get(environment.settings())`
2. Derive `SecretKeySpec("AES", keyBytes)`
3. Instantiate `FieldEncryptionService`; store on the companion object

**Key rotation support:**
- Implement `ReloadablePlugin` (like `NotificationCorePlugin.reload()`) so `_nodes/reload_secure_settings` hot-rotates the in-memory key without a restart
- Support an optional `opensearch.notifications.field_encryption_key_previous` keystore entry so existing ciphertexts can still be decrypted during the rotation window (see Step 6)
- There is **no version counter cluster setting**; the `enc:v1:` format prefix is static for this format version and carries no key identity

**Passthrough mode:** If the keystore entry is absent, `FieldEncryptionService` operates as a no-op and emits a `WARN` log. This allows zero-downtime rollout.

---

## Step 4 — Wire Encryption into the Index Read/Write Lifecycle

Create `ConfigEncryptionTransformer` in `notifications/util/`:
- `encryptConfig(config: NotificationConfig): NotificationConfig` — reconstructs each config data object with encrypted field values; only touches annotated sensitive fields; all other fields pass through unchanged
- `decryptConfig(config: NotificationConfig): NotificationConfig` — reverse; plaintext-prefixed values pass through silently

**Wire into `NotificationConfigIndex`:**

| Path | Action |
|---|---|
| **Write** — `createNotificationConfig()`, `updateNotificationConfig()` | Call `transformer.encryptConfig(config)` before `toXContent()` |
| **Read** — `parseNotificationConfigDoc()` (used by single-get and search) | Call `transformer.decryptConfig(doc.config)` after `parse()` |

No changes to `NotificationConfig.toXContent()` or the `core-spi` models are needed. No index mapping changes are required — ciphertext is valid text. A minor version bump in `notifications-config-mapping.yml` plus an `_encrypted_fields` metadata keyword field (listing which fields are currently encrypted) improves operator visibility.

---

## Step 5 — Migration Strategy for Existing Plaintext Records

**Transparent lazy migration:**
The `enc:v1:` sentinel prefix means existing plaintext records decode correctly with no crash or data loss. Every time a config is updated, it is automatically re-saved with encrypted values.

**Proactive bulk migration:**
Add a new admin-only REST endpoint:
```
POST /_plugins/_notifications/configs/_reencrypt
```
- Pages through all configs, applies `decrypt → re-encrypt`, re-saves via `updateNotificationConfig`
- Idempotent — skips already-encrypted fields via `isEncrypted()`
- Gated behind a new privilege: `cluster:admin/opensearch/notifications/configs/reencrypt`

**Feature flag:**
Add a cluster setting:
```
opensearch.notifications.general.encryption_enabled (NodeScope, Dynamic, default: false)
```
When `false`, the transformer is a no-op (safe pre-key-provisioning). Flip to `true` after the key is provisioned on all nodes, then optionally run the re-encrypt task.

---

## Step 6 — Key Rotation, REST Masking & Audit Logging

### Key Rotation

Two **fixed** keystore entry names are the sole mechanism for key rotation. The names never change; only the values do.

| Keystore Entry | Role |
|---|---|
| `opensearch.notifications.field_encryption_key` | **Active key** — used for all new encryptions |
| `opensearch.notifications.field_encryption_key_previous` | **Previous key** — present only during the rotation window; used for decryption only |

**Rotation steps (zero-downtime):**

1. On every node, set the old key value as `field_encryption_key_previous` and the new key value as `field_encryption_key`.
2. Call `POST /_nodes/reload_secure_settings` — all nodes now decrypt with either key, encrypt with the new key.
3. Call `POST /_plugins/_notifications/configs/_reencrypt` — re-saves every config through `decrypt(active or previous) → encrypt(active)`. After this, no ciphertext was produced by the old key.
4. On every node, remove `field_encryption_key_previous` from the keystore.
5. Call `POST /_nodes/reload_secure_settings` again — rotation window is closed.

**`FieldEncryptionService` decrypt logic:**

```kotlin
fun decrypt(value: String): String {
    if (!isEncrypted(value)) return value   // plaintext pass-through (backward compat)
    return tryDecrypt(activeKey, value)
        ?: previousKey?.let { tryDecrypt(it, value) }
        ?: throw DecryptionException("Decryption failed — check key configuration for config")
}
```

No version-to-key mapping is needed. `activeKey` is tried first (common path); `previousKey` is only present during the rotation window. There is no cluster setting to bump and no version tag in the ciphertext that carries key identity — `enc:v1:` is a **format** version only.


### REST Response Masking

Two options (pick one as starting point):

| Option | Behaviour | Trade-off |
|---|---|---|
| **(A) Return decrypted values** | GET response shows plaintext to authenticated caller | Simple; supports edit round-trips |
| **(B) Mask by default; `_reveal` endpoint** | GET returns `"***"` for sensitive fields; separate `GET /configs/{id}/_reveal` requires stricter privilege | Better least-privilege; more implementation work |

**Recommendation:** Start with Option A; add Option B as a follow-up security hardening item.

### Audit Logging

- `WARN` on any decryption failure (log config ID + field path, **never** values)
- `DEBUG` structured log entries in `ConfigIndexingActions` marking which config IDs triggered encrypt/decrypt — usable for compliance audit trails

---

## Cross-Cutting Considerations

### 1 — Module Placement
The index-level encryption key belongs exclusively to the `notifications` module (storage layer). The SMTP `SecureSettings` already live in `core/`. Keep them separate unless `core` ever needs to process sensitive field values — at that point promote `FieldEncryptionService` to `core-spi`.

### 2 — Searchability Trade-off
Encrypting `url` fields with a unique IV per write makes them **opaque to OpenSearch queries**. `ConfigQueryHelper` currently supports URL-based text filters — these will stop working on encrypted documents. Mitigation options:
- Store a **deterministic HMAC** (keyed separately) alongside ciphertext in a `keyword` field for equality-match queries
- Or accept that URL-based config queries are disabled when encryption is on and document this explicitly

### 3 — Cluster-Wide Key Distribution
`SecureSetting` keystore entries are **node-local**. In a multi-node cluster, all nodes must have the same `field_encryption_key` provisioned before enabling encryption. Rollout runbook:
1. `opensearch-keystore add opensearch.notifications.field_encryption_key` on every node
2. Rolling restart or `_nodes/reload_secure_settings`
3. Flip `encryption_enabled` cluster setting to `true`
4. Run `POST /_plugins/_notifications/configs/_reencrypt`
