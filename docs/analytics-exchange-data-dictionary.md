# Analytics Exchange Data Dictionary

All objects below are published under `smartbi_exchange`. T05 publishes dimensions, three implemented facts, operational trace tables, and empty reserved contracts for T07/T08. Values in `mastery_probability`, `confidence`, `forgetting_risk`, and `weakness_score` come from T02 state tables; SmartBI MUST NOT recompute them.

| Object | Grain | Column contract and source | SmartBI guidance |
|---|---|---|---|
| `sb_dim_course` | one row per course | `course_id` varchar not null, `course_name` varchar not null, `data_origin`, `source_version`; source `app.courses` | count distinct `course_id`; do not sum names |
| `sb_dim_class` | one row per class | `class_id`, `class_name`, `course_id`, `data_origin`, `source_version`; source `app.classrooms` | join to course by `course_id` |
| `sb_dim_student` | one row per student | `student_id`, `display_name`, `class_id`, `course_id`, `data_origin`, `source_version`; source `app.students` | count distinct students |
| `sb_dim_knowledge_point` | one row per course knowledge point | `knowledge_point_id`, `display_name`, `course_id`, nullable `parent_knowledge_point_id`, `sort_order`, `data_origin`, `source_version`; source `app.knowledge_points` | join by stable id |
| `sb_fact_learning_state` | one row per student-course-knowledgePoint-state-snapshot | `snapshot_id`; ids; `mastery_probability`, `confidence`, `forgetting_risk`, nullable `weakness_score`; evidence/model versions; `snapshot_time`, `computed_at`; `snapshot_status`, `is_current`; trace/source fields | filter `is_current` for current state; timeline uses `snapshot_time`; never recompute T02 fields |
| `sb_fact_practice_attempt` | one row per authoritative PracticeAttempt | `attempt_id`, optional `practice_set_id`, student/course/class/question/knowledge point ids, `correct`, `response_time_ms`, `duration_seconds`, `attempt_time`, `attempt_index`, origin/version/trace, `is_active_demo`; source `app.practice_attempts` | accuracy is `AVG(correct)`; baseline and active live may coexist |
| `sb_fact_wrong_book` | one row per explicit WrongBookItem | `wrong_book_item_id`, student/course/class/question/knowledge point ids, `source_attempt_id`, `reason`, `status`, `review_count`, `added_at`, nullable `repaired_at`, origin/version/trace, `is_active_demo`; source `app.wrong_book_items` | count items, do not infer from incorrect attempts |
| `sb_data_freshness` | one row per published dataset | `dataset_key`, nullable `latest_source_event_time`, `latest_projection_time`, `observed_at`, `row_count`, `source_version` | display source and observation timestamps separately |
| `sb_demo_run_state` | one row per demo run | `demo_run_id`, `demo_case_id`, `status`, `started_at`, nullable `reset_at`, `active`, `correlation_id`, `source_version` | active analysis selects `active = true` |
| `sb_fact_diagnosis` | reserved, no T05 rows | T07 contract placeholder | do not aggregate until T07 |
| `sb_fact_intervention` | reserved, no T05 rows | T07 contract placeholder | do not aggregate until T07 |
| `sb_fact_intervention_outcome` | reserved, no T05 rows | T08 contract placeholder | do not aggregate until T08 |

`data_origin` is `BASELINE_SIMULATED`, `LIVE_DEMO`, or `PRODUCTION`. Active-demo filtering is represented explicitly by `is_active_demo` on live practice and wrong-book facts; reset runs remain traceable but are not active analysis rows. The exchange contains no passwords, tokens, credentials, or secrets.
