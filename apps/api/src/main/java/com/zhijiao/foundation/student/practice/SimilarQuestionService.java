package com.zhijiao.foundation.student.practice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.knowledge.Citation;
import com.zhijiao.foundation.knowledge.KnowledgeQueryPort;
import com.zhijiao.foundation.knowledge.KnowledgeSearchResult;
import com.zhijiao.foundation.knowledge.KnowledgeUnavailableException;
import com.zhijiao.foundation.student.coach.DiagnosticQuestion;
import com.zhijiao.foundation.student.coach.DiagnosticQuestionValidator;
import com.zhijiao.foundation.student.coach.DiagnosticTarget;
import com.zhijiao.foundation.student.coach.DiagnosticValidationException;
import com.zhijiao.foundation.student.coach.InvalidLlmOutputException;
import com.zhijiao.foundation.student.coach.LlmPort;
import com.zhijiao.foundation.student.coach.LlmRequest;
import com.zhijiao.foundation.student.coach.LlmResponse;
import com.zhijiao.foundation.student.coach.LlmTimeoutException;
import com.zhijiao.foundation.student.coach.LlmUnavailableException;
import com.zhijiao.foundation.student.coach.QuestionOption;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import com.zhijiao.foundation.student.learning.LearningStateView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SimilarQuestionService {
    private final PracticeRepository repository;
    private final LearningStateEngine learningStateEngine;
    private final KnowledgeQueryPort knowledgeQueryPort;
    private final LlmPort llmPort;
    private final ObjectMapper objectMapper;
    private final DiagnosticQuestionValidator validator;
    private final Clock clock;

    public SimilarQuestionService(PracticeRepository repository, LearningStateEngine learningStateEngine,
                                  KnowledgeQueryPort knowledgeQueryPort, LlmPort llmPort,
                                  ObjectMapper objectMapper, DiagnosticQuestionValidator validator, Clock clock) {
        this.repository = repository;
        this.learningStateEngine = learningStateEngine;
        this.knowledgeQueryPort = knowledgeQueryPort;
        this.llmPort = llmPort;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public PracticeSetView generate(String sessionId, String sourceAttemptId, int count) {
        if (count < 1 || count > 3) throw new IllegalArgumentException("count must be between 1 and 3");
        PracticeRepository.AttemptRow attempt = repository.findAttemptById(sourceAttemptId)
                .orElseThrow(() -> new PracticeAttemptNotFoundException(sourceAttemptId));
        if (sessionId != null && !sessionId.equals(attempt.coachSessionId())) {
            throw new DomainRuleViolationException("Attempt does not belong to coach session");
        }
        if (attempt.correct()) throw new DomainRuleViolationException("Similar questions require an incorrect attempt");
        ensureActiveDemo(attempt);
        InternalQuestion original = repository.findQuestion(attempt.practiceSetId(), attempt.questionId())
                .orElseThrow(() -> new DomainRuleViolationException("Original question is unavailable"));
        LearningStateView state = learningStateEngine == null ? null
                : learningStateEngine.read(attempt.studentId(), attempt.courseId(), attempt.knowledgePointId());
        List<KnowledgeSearchResult> evidence;
        try {
            evidence = knowledgeQueryPort.search(attempt.courseId(), attempt.knowledgePointId(), original.stem(), 4);
        } catch (KnowledgeUnavailableException exception) {
            evidence = List.of();
        }
        List<Citation> citations = evidence.stream().map(KnowledgeSearchResult::toCitation).toList();
        RuntimeException lastFailure = null;
        List<DiagnosticQuestion> questions = null;
        LlmResponse response = null;
        for (int retry = 0; retry < 2; retry++) {
            try {
                String stateHint = state == null ? "unavailable" : String.valueOf(state.state().masteryProbability());
                response = llmPort.complete(new LlmRequest(
                        "Generate structured practice questions as JSON. Never copy the original question.",
                        "parentQuestionId=" + original.questionId() + ", wrongAnswer=" + attempt.selectedAnswer()
                                + ", knowledgePointId=" + original.knowledgePointId() + ", mastery=" + stateHint
                                + ", evidence=" + evidence + ", count=" + count,
                        true));
                questions = parse(response, citations, original.questionId(), original.knowledgePointId(), count);
                validator.validate(questions, original.knowledgePointId(), count);
                if (questions.stream().anyMatch(question -> original.questionId().equals(question.questionId()))) {
                    throw new InvalidLlmOutputException("Similar question must have a new questionId", null);
                }
                break;
            } catch (LlmUnavailableException | LlmTimeoutException exception) {
                throw exception;
            } catch (DiagnosticValidationException | InvalidLlmOutputException exception) {
                lastFailure = exception;
            }
        }
        if (questions == null || response == null) {
            throw new InvalidLlmOutputException("Similar question output remained invalid after retry", lastFailure);
        }
        LlmResponse finalResponse = response;
        PracticeSet set = new PracticeSet("ps-similar-" + UUID.randomUUID().toString().replace("-", ""),
                attempt.studentId(), attempt.courseId(), attempt.classId(), sessionId, "AI_COACH_SIMILAR", "OPEN",
                attempt.demoRunId(), attempt.demoCaseId(), attempt.correlationId(), attempt.sourceVersion(), Instant.now(clock), null);
        List<PracticeRepository.GeneratedQuestion> generated = questions.stream().map(question ->
                new PracticeRepository.GeneratedQuestion(question.questionId(), question.knowledgePointId(), question.questionType(),
                        question.stem(), question.options().stream().map(option -> new QuestionOptionView(option.optionId(), option.text())).toList(),
                        question.correctAnswer(), question.explanation(), question.diagnosticTarget(), question.difficulty(),
                        original.questionId(), finalResponse.provider(), finalResponse.modelVersion(), finalResponse.promptVersion(), citations)).toList();
        repository.insertGeneratedPracticeSet(set, generated);
        return new PracticeSetView(set, generated.stream().map(item -> StudentQuestion.from(item.toInternal(set.practiceSetId()))).toList(), List.of());
    }

    private void ensureActiveDemo(PracticeRepository.AttemptRow attempt) {
        if (!"LIVE_DEMO".equals(attempt.dataOrigin()) || attempt.demoRunId() == null) {
            return;
        }
        PracticeRepository.DemoContext activeDemo = repository.findActiveDemo(attempt.studentId(), attempt.courseId())
                .orElseThrow(() -> new DomainRuleViolationException("An active demo run is required for similar questions"));
        if (!attempt.demoRunId().equals(activeDemo.demoRunId())) {
            throw new DomainRuleViolationException("Attempt belongs to a reset demo run");
        }
    }

    private List<DiagnosticQuestion> parse(LlmResponse response, List<Citation> citations, String parentQuestionId,
                                           String knowledgePointId, int count) {
        try {
            JsonNode root = objectMapper.readTree(response.content());
            JsonNode array = root.isArray() ? root : root.path("questions");
            if (!array.isArray() || array.size() != count) throw new InvalidLlmOutputException("Expected exact similar question count", null);
            List<DiagnosticQuestion> result = new ArrayList<>();
            for (JsonNode node : array) {
                List<QuestionOption> options = new ArrayList<>();
                for (JsonNode option : node.path("options")) {
                    options.add(new QuestionOption(option.path("optionId").asText(null), option.path("text").asText(null)));
                }
                JsonNode target = node.path("diagnosticTarget");
                result.add(new DiagnosticQuestion(node.path("questionId").asText(null), knowledgePointId,
                        node.path("questionType").asText(null), node.path("stem").asText(null), options,
                        node.path("correctAnswer").asText(null), node.path("explanation").asText(null),
                        new DiagnosticTarget(target.path("code").asText("SIMILAR_PRACTICE"),
                                target.path("description").asText("针对原题误答生成的变式练习")),
                        node.path("difficulty").asDouble(Double.NaN), citations,
                        response.provider(), response.modelVersion(), response.promptVersion()));
            }
            return result;
        } catch (InvalidLlmOutputException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidLlmOutputException("Similar question output is not valid JSON", exception);
        }
    }
}
