package com.zhijiao.foundation.student.coach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.knowledge.Citation;
import com.zhijiao.foundation.knowledge.KnowledgeQueryPort;
import com.zhijiao.foundation.knowledge.KnowledgeSearchResult;
import com.zhijiao.foundation.knowledge.KnowledgeUnavailableException;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import com.zhijiao.foundation.student.learning.LearningStateView;
import com.zhijiao.foundation.student.learning.StudentKnowledgeState;
import com.zhijiao.foundation.student.learning.WeakKnowledgePointCandidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CoachOrchestrator {
    private final LearningStateEngine learningStateEngine;
    private final KnowledgeQueryPort knowledgeQueryPort;
    private final LlmPort llmPort;
    private final CoachRepository repository;
    private final ObjectMapper objectMapper;
    private final DiagnosticQuestionValidator validator;
    private final int maxDiagnosticRetries;
    private final String promptVersion;
    private final Clock clock;

    @Autowired
    public CoachOrchestrator(LearningStateEngine learningStateEngine, KnowledgeQueryPort knowledgeQueryPort,
                             LlmPort llmPort, CoachRepository repository, ObjectMapper objectMapper,
                             DiagnosticQuestionValidator validator, CoachProperties properties, Clock clock) {
        this(learningStateEngine, knowledgeQueryPort, llmPort, repository, objectMapper, validator,
                properties.getLlm().getMaxDiagnosticRetries(), properties.getLlm().getPromptVersion(), clock);
    }

    public CoachOrchestrator(LearningStateEngine learningStateEngine, KnowledgeQueryPort knowledgeQueryPort,
                             LlmPort llmPort, CoachRepository repository, ObjectMapper objectMapper,
                             DiagnosticQuestionValidator validator, int maxDiagnosticRetries, String promptVersion) {
        this(learningStateEngine, knowledgeQueryPort, llmPort, repository, objectMapper, validator,
                maxDiagnosticRetries, promptVersion, Clock.systemUTC());
    }

    private CoachOrchestrator(LearningStateEngine learningStateEngine, KnowledgeQueryPort knowledgeQueryPort,
                              LlmPort llmPort, CoachRepository repository, ObjectMapper objectMapper,
                              DiagnosticQuestionValidator validator, int maxDiagnosticRetries, String promptVersion,
                              Clock clock) {
        this.learningStateEngine = learningStateEngine;
        this.knowledgeQueryPort = knowledgeQueryPort;
        this.llmPort = llmPort;
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.maxDiagnosticRetries = Math.max(0, maxDiagnosticRetries);
        this.promptVersion = promptVersion == null || promptVersion.isBlank() ? "coach-prompt-v1" : promptVersion;
        this.clock = clock;
    }

    @Transactional
    public CoachSession createSession(String studentId, String courseId, String knowledgePointId,
                                      String mode, String idempotencyKey) {
        CoachSession existing = repository.findSessionByIdempotencyKey(studentId, idempotencyKey).orElse(null);
        if (existing != null) return existing;
        LearningStateView view = learningStateEngine.read(studentId, courseId, knowledgePointId);
        StudentKnowledgeState state = view.state();
        WeakKnowledgePointCandidate candidate = view.weakKnowledgePoints().stream()
                .filter(item -> item.knowledgePointId().equals(state.knowledgePointId()))
                .findFirst().orElse(null);
        Instant now = Instant.now(clock);
        CoachSession session = new CoachSession(
                "coach-" + UUID.randomUUID().toString().replace("-", ""), studentId, courseId,
                state.knowledgePointId(), normalizeMode(mode), "ACTIVE", RagStatus.EMPTY,
                state.masteryProbability(), state.confidence(), state.forgettingRisk(),
                candidate == null ? 0.0 : candidate.weaknessScore(),
                candidate == null ? "" : String.join(",", candidate.reasonCodes()),
                state.masteryModelVersion() + "/" + state.abilityModelVersion(), state.sourceVersion(), now, now);
        repository.insertSession(session, idempotencyKey);
        return session;
    }

    @Transactional(readOnly = true)
    public CoachSessionView getSession(String sessionId) {
        CoachSession session = repository.findSession(sessionId).orElseThrow(() -> new CoachSessionNotFoundException(sessionId));
        return new CoachSessionView(session, repository.findMessages(sessionId), repository.findDiagnosticQuestions(sessionId));
    }

    @Transactional(noRollbackFor = {LlmUnavailableException.class, LlmTimeoutException.class})
    public CoachMessageResult sendMessage(String sessionId, String message) {
        CoachSession session = repository.findSession(sessionId)
                .orElseThrow(() -> new CoachSessionNotFoundException(sessionId));
        if (message == null || message.isBlank()) throw new IllegalArgumentException("message is required");
        RagContext rag = retrieve(session.courseId(), session.knowledgePointId(), message);
        Instant now = Instant.now(clock);
        repository.insertMessage(new CoachMessage("msg-" + UUID.randomUUID().toString().replace("-", ""),
                sessionId, "USER", message, null, null, null, rag.status(), now, List.of()));
        LlmResponse response = llmPort.complete(new LlmRequest(
                "You are a course-aware student AI Coach. Explain clearly and do not invent course citations.",
                chatPrompt(session, message, rag.results()), false));
        List<Citation> citations = rag.results().stream().map(KnowledgeSearchResult::toCitation).toList();
        CoachMessage assistant = new CoachMessage("msg-" + UUID.randomUUID().toString().replace("-", ""), sessionId,
                "ASSISTANT", response.content(), response.provider(), response.modelVersion(), response.promptVersion(),
                rag.status(), Instant.now(clock), citations);
        repository.insertMessage(assistant);
        repository.insertMessageCitations(assistant);
        List<CoachMessageResult.CoachAction> actions = session.knowledgePointId() == null ? List.of()
                : List.of(new CoachMessageResult.CoachAction("START_DIAGNOSTIC", "用两道题确认一下"));
        return new CoachMessageResult(response.content(), citations, actions, rag.status());
    }

    @Transactional
    public DiagnosticSetResult generateDiagnosticSet(String sessionId, String studentId, String courseId,
                                                     String knowledgePointId) {
        CoachSession session = repository.findSession(sessionId)
                .orElseThrow(() -> new CoachSessionNotFoundException(sessionId));
        if (!session.studentId().equals(studentId) || !session.courseId().equals(courseId)) {
            throw new IllegalArgumentException("Session context does not match request");
        }
        LearningStateView view = learningStateEngine.read(studentId, courseId, knowledgePointId);
        RagContext rag = retrieve(courseId, knowledgePointId, "诊断 " + view.state().knowledgePointName());
        LlmResponse validatedResponse = null;
        List<DiagnosticQuestion> validatedQuestions = null;
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt <= maxDiagnosticRetries; attempt++) {
            try {
                LlmResponse candidateResponse = llmPort.complete(new LlmRequest(
                        "Generate exactly two SINGLE_CHOICE diagnostic questions as JSON. Do not include markdown.",
                        diagnosticPrompt(view, rag.results(), lastFailure == null ? null : lastFailure.getMessage()), true));
                List<DiagnosticQuestion> candidateQuestions = parseQuestions(candidateResponse, rag.results());
                validator.validate(candidateQuestions, knowledgePointId, 2);
                validatedResponse = candidateResponse;
                validatedQuestions = candidateQuestions;
                break;
            } catch (DiagnosticValidationException | InvalidLlmOutputException exception) {
                lastFailure = exception;
            }
        }
        if (validatedQuestions == null || validatedResponse == null) {
            throw new InvalidLlmOutputException("LLM diagnostic output remained invalid after retry", lastFailure);
        }
        String practiceSetId = "ps-diag-" + UUID.randomUUID().toString().replace("-", "");
        repository.saveDiagnosticSet(practiceSetId, sessionId, knowledgePointId, validatedQuestions, rag.status(), validatedResponse,
                view.state().sourceVersion());
        return new DiagnosticSetResult(practiceSetId, validatedQuestions, rag.status());
    }

    private RagContext retrieve(String courseId, String knowledgePointId, String query) {
        try {
            List<KnowledgeSearchResult> results = knowledgeQueryPort.search(courseId, knowledgePointId, query, 4);
            if (results == null || results.isEmpty()) return new RagContext(RagStatus.EMPTY, List.of());
            return new RagContext(RagStatus.INDEXED, List.copyOf(results));
        } catch (KnowledgeUnavailableException exception) {
            return new RagContext(RagStatus.DEGRADED, List.of());
        }
    }

    private List<DiagnosticQuestion> parseQuestions(LlmResponse response, List<KnowledgeSearchResult> evidence) {
        try {
            JsonNode root = objectMapper.readTree(response.content());
            JsonNode array = root.isArray() ? root : root.path("questions");
            if (!array.isArray()) throw new InvalidLlmOutputException("Diagnostic output must contain questions array", null);
            List<Citation> citations = evidence.stream().map(KnowledgeSearchResult::toCitation).toList();
            List<DiagnosticQuestion> questions = new ArrayList<>();
            for (JsonNode node : array) {
                List<QuestionOption> options = new ArrayList<>();
                for (JsonNode option : node.path("options")) {
                    options.add(new QuestionOption(option.path("optionId").asText(null), option.path("text").asText(null)));
                }
                JsonNode target = node.path("diagnosticTarget");
                questions.add(new DiagnosticQuestion(node.path("questionId").asText(null),
                        node.path("knowledgePointId").asText(null), node.path("questionType").asText(null),
                        node.path("stem").asText(null), options, node.path("correctAnswer").asText(null),
                        node.path("explanation").asText(null), new DiagnosticTarget(target.path("code").asText(null),
                        target.path("description").asText(null)), node.path("difficulty").asDouble(Double.NaN),
                        citations, response.provider(), response.modelVersion(), response.promptVersion()));
            }
            return questions;
        } catch (InvalidLlmOutputException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidLlmOutputException("Diagnostic output is not valid JSON", exception);
        }
    }

    private String chatPrompt(CoachSession session, String message, List<KnowledgeSearchResult> evidence) {
        return "studentId=" + session.studentId() + ", courseId=" + session.courseId()
                + ", knowledgePointId=" + session.knowledgePointId() + ", mastery=" + session.mastery()
                + ", confidence=" + session.confidence() + ", forgettingRisk=" + session.forgettingRisk()
                + ", userMessage=" + message + ", evidence=" + evidence;
    }

    private String diagnosticPrompt(LearningStateView view, List<KnowledgeSearchResult> evidence,
                                    String previousValidationFailure) {
        String retryInstruction = previousValidationFailure == null ? "" : """

                Previous output failed contract validation: %s
                Regenerate the entire JSON object following the exact schema.
                """.formatted(previousValidationFailure);
        return """
                Generate exactly two diagnostic questions for the requested knowledge point.
                Return only one JSON object with no markdown and no extra wrapper:
                {
                  "questions": [
                    {
                      "questionId": "q-1",
                      "knowledgePointId": "<exact requested knowledgePointId>",
                      "questionType": "SINGLE_CHOICE",
                      "stem": "...",
                      "options": [
                        {"optionId": "A", "text": "..."},
                        {"optionId": "B", "text": "..."}
                      ],
                      "correctAnswer": "A",
                      "explanation": "...",
                      "diagnosticTarget": {"code": "...", "description": "..."},
                      "difficulty": 0.5
                    },
                    {
                      "questionId": "q-2",
                      "knowledgePointId": "<exact requested knowledgePointId>",
                      "questionType": "SINGLE_CHOICE",
                      "stem": "...",
                      "options": [
                        {"optionId": "A", "text": "..."},
                        {"optionId": "B", "text": "..."}
                      ],
                      "correctAnswer": "A",
                      "explanation": "...",
                      "diagnosticTarget": {"code": "...", "description": "..."},
                      "difficulty": 0.5
                    }
                  ]
                }

                Hard constraints:
                - exactly 2 questions
                - questionId is required, unique, and nonblank
                - knowledgePointId MUST exactly equal the requested knowledgePointId
                - questionType MUST equal SINGLE_CHOICE
                - stem, explanation, diagnosticTarget.code, and diagnosticTarget.description are required and nonblank
                - every question has at least 2 options with unique, nonblank optionId and option text
                - correctAnswer MUST equal an optionId
                - difficulty is a finite number in the inclusive range [0.0, 1.0]
                - required fields must not be null
                - do not include extra top-level fields or markdown
                - use the supplied RAG evidence and do not invent citations

                Requested knowledgePointId: %s
                Knowledge point name: %s
                Learning context: mastery=%s, confidence=%s, weaknessScore=%s
                RAG evidence: %s%s
                """.formatted(view.state().knowledgePointId(), view.state().knowledgePointName(),
                view.state().masteryProbability(), view.state().confidence(),
                view.weakKnowledgePoints().stream().findFirst().map(WeakKnowledgePointCandidate::weaknessScore).orElse(0.0),
                evidence, retryInstruction);
    }

    private String normalizeMode(String mode) {
        String normalized = mode == null || mode.isBlank() ? "TUTOR" : mode.trim().toUpperCase();
        if (!normalized.equals("TUTOR") && !normalized.equals("DIAGNOSTIC")) {
            throw new IllegalArgumentException("Unsupported coach mode");
        }
        return normalized;
    }

    private record RagContext(RagStatus status, List<KnowledgeSearchResult> results) {
    }
}
