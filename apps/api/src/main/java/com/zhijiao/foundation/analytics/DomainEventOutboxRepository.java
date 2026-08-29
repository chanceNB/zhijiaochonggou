package com.zhijiao.foundation.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Appends domain facts in the same transaction as the authoritative write. */
@Repository
public class DomainEventOutboxRepository {
    private final JdbcTemplate jdbcTemplate;

    public DomainEventOutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void append(String aggregateType, String aggregateId, String eventType,
                       Instant occurredAt, String sourceVersion, String dataOrigin,
                       String demoRunId, String demoCaseId, String correlationId) {
        String eventId = "outbox-" + UUID.randomUUID().toString().replace("-", "");
        jdbcTemplate.update("""
                insert into app.domain_event_outbox
                    (event_id, aggregate_type, aggregate_id, event_type, occurred_at, source_version,
                     data_origin, demo_run_id, demo_case_id, correlation_id, created_at)
                select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.domain_event_outbox where event_id = ?)
                """, eventId, aggregateType, aggregateId, eventType, timestamp(occurredAt), sourceVersion,
                dataOrigin, demoRunId, demoCaseId, correlationId, timestamp(Instant.now()), eventId);
    }

    public long pendingCount() {
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from app.domain_event_outbox where published_at is null", Long.class);
        return count == null ? 0 : count;
    }

    public int markPublished(Instant publishedAt) {
        return jdbcTemplate.update("update app.domain_event_outbox set published_at = ? where published_at is null",
                timestamp(publishedAt));
    }

    private OffsetDateTime timestamp(Instant value) {
        return value.atOffset(ZoneOffset.UTC);
    }
}
