CREATE TABLE IF NOT EXISTS app.knowledge_documents (
    document_id VARCHAR(160) PRIMARY KEY,
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    title VARCHAR(300) NOT NULL,
    source VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN ('UPLOADED', 'INDEXED', 'FAILED')),
    data_origin VARCHAR(32) NOT NULL,
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    indexed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS app.knowledge_chunks (
    chunk_id VARCHAR(200) PRIMARY KEY,
    document_id VARCHAR(160) NOT NULL REFERENCES app.knowledge_documents(document_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    knowledge_point_id VARCHAR(128) REFERENCES app.knowledge_points(knowledge_point_id),
    title VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    chunk_index INTEGER NOT NULL CHECK (chunk_index >= 0),
    embedding TEXT NOT NULL,
    metadata TEXT,
    source_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (document_id, chunk_index)
);

CREATE INDEX IF NOT EXISTS idx_knowledge_chunks_course_kp
    ON app.knowledge_chunks (course_id, knowledge_point_id);

CREATE TABLE IF NOT EXISTS app.coach_sessions (
    session_id VARCHAR(160) PRIMARY KEY,
    student_id VARCHAR(128) NOT NULL REFERENCES app.students(student_id),
    course_id VARCHAR(128) NOT NULL REFERENCES app.courses(course_id),
    knowledge_point_id VARCHAR(128) REFERENCES app.knowledge_points(knowledge_point_id),
    mode VARCHAR(32) NOT NULL CHECK (mode IN ('TUTOR', 'DIAGNOSTIC')),
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'CLOSED')),
    rag_status VARCHAR(32) NOT NULL,
    mastery NUMERIC(5,4) NOT NULL CHECK (mastery >= 0 AND mastery <= 1),
    confidence NUMERIC(5,4) NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
    forgetting_risk NUMERIC(5,4) NOT NULL CHECK (forgetting_risk >= 0 AND forgetting_risk <= 1),
    weakness_score NUMERIC(8,6) NOT NULL CHECK (weakness_score >= 0 AND weakness_score <= 1),
    reason_codes VARCHAR(512) NOT NULL,
    learning_model_version VARCHAR(128) NOT NULL,
    source_version VARCHAR(64) NOT NULL,
    idempotency_key VARCHAR(200),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (student_id, idempotency_key)
);

CREATE TABLE IF NOT EXISTS app.coach_messages (
    message_id VARCHAR(160) PRIMARY KEY,
    session_id VARCHAR(160) NOT NULL REFERENCES app.coach_sessions(session_id),
    message_type VARCHAR(32) NOT NULL CHECK (message_type IN ('USER', 'ASSISTANT', 'ACTION', 'DIAGNOSTIC')),
    content TEXT NOT NULL,
    model_provider VARCHAR(128),
    model_version VARCHAR(128),
    prompt_version VARCHAR(128),
    rag_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_coach_messages_session_time
    ON app.coach_messages (session_id, created_at, message_id);

CREATE TABLE IF NOT EXISTS app.coach_diagnostic_questions (
    practice_set_id VARCHAR(160) NOT NULL,
    question_id VARCHAR(160) NOT NULL,
    session_id VARCHAR(160) NOT NULL REFERENCES app.coach_sessions(session_id),
    knowledge_point_id VARCHAR(128) NOT NULL REFERENCES app.knowledge_points(knowledge_point_id),
    question_type VARCHAR(32) NOT NULL CHECK (question_type = 'SINGLE_CHOICE'),
    stem TEXT NOT NULL,
    options TEXT NOT NULL,
    correct_answer VARCHAR(128) NOT NULL,
    explanation TEXT NOT NULL,
    diagnostic_target TEXT NOT NULL,
    difficulty NUMERIC(5,4) NOT NULL CHECK (difficulty >= 0 AND difficulty <= 1),
    model_provider VARCHAR(128) NOT NULL,
    model_version VARCHAR(128) NOT NULL,
    prompt_version VARCHAR(128) NOT NULL,
    source_version VARCHAR(64) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (practice_set_id, question_id)
);

CREATE TABLE IF NOT EXISTS app.coach_citations (
    citation_id VARCHAR(160) PRIMARY KEY,
    session_id VARCHAR(160) NOT NULL REFERENCES app.coach_sessions(session_id),
    message_id VARCHAR(160) REFERENCES app.coach_messages(message_id),
    practice_set_id VARCHAR(160),
    question_id VARCHAR(160),
    document_id VARCHAR(160) NOT NULL REFERENCES app.knowledge_documents(document_id),
    chunk_id VARCHAR(200) NOT NULL REFERENCES app.knowledge_chunks(chunk_id),
    title VARCHAR(300) NOT NULL,
    excerpt TEXT NOT NULL,
    score NUMERIC(8,6) NOT NULL CHECK (score >= 0 AND score <= 1),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_coach_citations_session
    ON app.coach_citations (session_id, created_at);
