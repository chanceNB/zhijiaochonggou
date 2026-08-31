# F03 Deferred Architecture Issues

## Diagnosis persistence gap

`GET /api/v1/teacher/diagnosis-cases/{caseId}` currently treats `caseId` as the
active `demoCaseId` and returns a teacher read projection composed from the
active learning snapshot and active-demo practice attempts.

The authoritative contracts assign ownership of `DiagnosisCase` and `Evidence`
to the Teacher domain and require persisted `DiagnosisFact` trace fields. The
repository currently has no B-owned `DiagnosisCase` or `Evidence` tables. The
`smartbi_exchange.sb_fact_diagnosis` table is explicitly reserved and contains
no authoritative application rows.

Therefore this endpoint is a useful Golden Demo read projection, but it is not
an authoritative persisted DiagnosisCase. This is intentionally deferred from
F03 because resolving it requires a domain and persistence decision rather than
a read-model scope fix.

Minimal follow-up: add B-owned persisted `DiagnosisCase` and `Evidence` records
with `caseId`, `studentId`, `courseId`, `classId`, `knowledgePointId`, status,
hypothesis, evidence references, and the required `demoRunId`, `demoCaseId`,
`correlationId`, `sourceVersion`, and event timestamps. Then make the endpoint
read those facts and keep the current projection as a separate derived view.

`DIAGNOSIS_DOMAIN_GAP = CONFIRMED`
