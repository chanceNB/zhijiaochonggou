package com.zhijiao.foundation.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DemoRunServiceTest {

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Autowired
    private DemoRunService demoRunService;

    @Test
    void createAndTraceRunUsesStableDemoContext() {
        baselineSeedService.seed();

        DemoRun run = demoRunService.create("DEMO-GRAPH-001", "baseline-ds-v1");
        DemoRunTrace trace = demoRunService.trace(run.demoRunId());

        assertThat(run.status()).isEqualTo("ACTIVE");
        assertThat(run.studentId()).isEqualTo("stu-xiaoming");
        assertThat(trace.demoCaseId()).isEqualTo("DEMO-GRAPH-001");
        assertThat(trace.baselineVersion()).isEqualTo("baseline-ds-v1");
        assertThat(trace.correlationId()).isEqualTo(run.correlationId());
    }

    @Test
    void createRejectsUnknownBaselineVersion() {
        assertThatThrownBy(() -> demoRunService.create("DEMO-GRAPH-001", "missing-baseline"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
