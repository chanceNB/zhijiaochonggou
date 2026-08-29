# Database Contracts

`smartbi_exchange` is the only schema published to SmartBI. `app` remains the operational and algorithm fact owner.

Run `smartbi_reader_provision.sql` as a PostgreSQL administrator with a deployment-only password variable. The script is repeatable and grants the `smartbi_reader` role `CONNECT`, `USAGE` on `smartbi_exchange`, and `SELECT` on published tables only. It explicitly revokes access to `app` and all write privileges on the exchange schema.

T05 uses projection tables rather than views. `AnalyticsProjectionService.refresh()` rebuilds the exchange in one transaction from the authoritative `app` rows, then marks the outbox events observed. Repeating the refresh is idempotent. `sb_data_freshness.latest_source_event_time` is the latest source business timestamp; `latest_projection_time` and `observed_at` are the actual observation time, not a fabricated source timestamp.
