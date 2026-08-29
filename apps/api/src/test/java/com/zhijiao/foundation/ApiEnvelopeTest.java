package com.zhijiao.foundation;

import com.zhijiao.foundation.api.ApiEnvelope;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ApiEnvelopeTest {

    @Test
    void successEnvelopeContainsContractFields() {
        Instant timestamp = Instant.parse("2026-08-29T08:00:00Z");

        ApiEnvelope<String> envelope = ApiEnvelope.success("req-123", "ok", timestamp);

        assertThat(envelope.code()).isEqualTo("OK");
        assertThat(envelope.message()).isEqualTo("success");
        assertThat(envelope.requestId()).isEqualTo("req-123");
        assertThat(envelope.data()).isEqualTo("ok");
        assertThat(envelope.details()).isNull();
        assertThat(envelope.timestamp()).isEqualTo(timestamp);
    }
}
