package com.zhijiao.foundation.demo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DemoRunService {
    private final BaselineRepository repository;
    private final Clock clock;

    public DemoRunService(BaselineRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public DemoRun create(String demoCaseId, String baselineVersion) {
        return createRun(validateContext(demoCaseId, baselineVersion), null);
    }

    @Transactional
    public DemoRun reset(String demoRunId) {
        DemoRun current = repository.findDemoRun(demoRunId)
                .orElseThrow(() -> new DemoRunNotFoundException(demoRunId));
        if (!"ACTIVE".equals(current.status())) {
            throw new IllegalStateException("Only an active demo run can be reset");
        }
        Instant now = Instant.now(clock);
        repository.markReset(current.demoRunId(), now);
        return createRun(current, current.demoRunId());
    }

    @Transactional(readOnly = true)
    public DemoRun traceRun(String demoRunId) {
        return repository.findDemoRun(demoRunId)
                .orElseThrow(() -> new DemoRunNotFoundException(demoRunId));
    }

    @Transactional(readOnly = true)
    public DemoRunTrace trace(String demoRunId) {
        DemoRun run = traceRun(demoRunId);
        Map<String, String> refs = Map.of(
                "studentId", run.studentId(),
                "courseId", run.courseId(),
                "classId", run.classId()
        );
        List<DemoTraceEvent> events = repository.findTraceEvents(run.demoRunId());
        return new DemoRunTrace(run.demoRunId(), run.demoCaseId(), run.baselineVersion(),
                run.correlationId(), run.stage(), refs, events);
    }

    private DemoRun validateContext(String demoCaseId, String baselineVersion) {
        if (!BaselineSeedService.DEMO_CASE_ID.equals(demoCaseId)) {
            throw new IllegalArgumentException("Unsupported demo case");
        }
        if (!repository.findBaselineVersion(baselineVersion).isPresent()) {
            throw new IllegalArgumentException("Unknown baseline version");
        }
        BaselineRepository.StudentSeedContext student = repository.findStudent(BaselineSeedService.XIAOMING_ID)
                .orElseThrow(() -> new IllegalArgumentException("Baseline student is missing"));
        return new DemoRun("", demoCaseId, baselineVersion, student.studentId(), student.courseId(),
                student.classId(), "ACTIVE", "BASELINE_READY", "", null, null, null);
    }

    private DemoRun createRun(DemoRun context, String resetFromDemoRunId) {
        Instant now = Instant.now(clock);
        DemoRun run = new DemoRun(
                "demo-run-" + UUID.randomUUID().toString().replace("-", ""),
                context.demoCaseId(),
                context.baselineVersion(),
                context.studentId(),
                context.courseId(),
                context.classId(),
                "ACTIVE",
                "BASELINE_READY",
                "corr-" + UUID.randomUUID().toString().replace("-", ""),
                now,
                null,
                resetFromDemoRunId
        );
        repository.insertDemoRun(run);
        return run;
    }
}
