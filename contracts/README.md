# Messaging Contracts

These JSON Schemas describe the broker-level contracts between the Wallet, Transfer, and Notification services. They are technology neutral so each service (or language) can validate payloads before publishing/consuming messages.

## Envelope

All messages include the fields defined in [`json/message-envelope.schema.json`](json/message-envelope.schema.json):

- `messageId` – unique UUID for deduplication.
- `occurredAt` – ISO timestamp for domain occurrence time.
- `correlationId` – saga/request identifier propagated end-to-end.
- `causationId` – (optional) pointer back to the triggering message.
- `traceId` – (optional) distributed tracing identifier.
- `messageType` – routing key/type on the broker (e.g., `wallet.debit.command`).
- `payloadVersion` – semantic version of the payload schema (e.g., `1.0.0`).

Producers MUST set `messageType` to the routing key listed below and send the payload in JSON format. Typical AMQP metadata should duplicate `messageId`, `correlationId`, and `messageType` headers for easier filtering by other stacks.

## Command & Event Schemas

| Schema | Description | Publisher ➜ Consumer | Routing Key (`messageType`) |
| --- | --- | --- | --- |
| [`json/wallet-debit-command.schema.json`](json/wallet-debit-command.schema.json) | Requests the wallet service to debit an account as part of a transfer saga. | Transfer ➜ Wallet | `wallet.debit.command` |
| [`json/wallet-credit-command.schema.json`](json/wallet-credit-command.schema.json) | Requests the wallet service to credit an account. | Transfer ➜ Wallet | `wallet.credit.command` |
| [`json/wallet-transaction-result-event.schema.json`](json/wallet-transaction-result-event.schema.json) | Announces the success/failure of a debit or credit command, including ledger entry IDs. | Wallet ➜ Transfer | `wallet.transaction.result` |
| [`json/transfer-completed-event.schema.json`](json/transfer-completed-event.schema.json) | Signals that a transfer saga fully succeeded. | Transfer ➜ Notification | `transfer.completed.event` |
| [`json/transfer-failed-event.schema.json`](json/transfer-failed-event.schema.json) | Signals that a transfer saga failed and includes diagnostic context. | Transfer ➜ Notification | `transfer.failed.event` |

## Versioning

- Increment the **patch** version in `payloadVersion` when fixing backwards-compatible bugs/documentation.
- Increment the **minor** version when adding optional fields.
- Increment the **major** version when removing/renaming fields or making incompatible changes. Producers and consumers should negotiate supported versions via headers or routing keys (e.g., `wallet.debit.command.v2`).

Each schema file should include an updated `$id` and `payloadVersion` default in examples/tests when new versions are published.

## Validation

Use your stack’s JSON Schema validator before publishing or after consuming messages. During development you can run:

```bash
docker run --rm -v "$PWD/contracts/json:/schemas" ghcr.io/ajv-validator/ajv-cli:v6 \
  ajv validate -s /schemas/wallet-debit-command.schema.json \
               -d /schemas/examples/wallet-debit-command.json
```

(Add example payloads under `contracts/examples/` as needed.)
