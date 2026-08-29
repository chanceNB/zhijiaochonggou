package com.zhijiao.foundation.analytics;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class AnalyticsProjectionService {
    private final AnalyticsProjectionRepository repository;
    private final DomainEventOutboxRepository outbox;
    private final Clock clock;

    public AnalyticsProjectionService(AnalyticsProjectionRepository repository, DomainEventOutboxRepository outbox, Clock clock) {
        this.repository = repository;
        this.outbox = outbox;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Transactional
    public AnalyticsProjectionResult refresh() {
        Instant observedAt = Instant.now(clock);
        AnalyticsProjectionResult result = repository.rebuild(observedAt);
        outbox.markPublished(observedAt);
        return result;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDataset> catalog() {
        return repository.catalog();
    }

    @Transactional(readOnly = true)
    public List<AnalyticsFreshness> freshness() {
        return repository.freshness(Instant.now(clock));
    }
}
