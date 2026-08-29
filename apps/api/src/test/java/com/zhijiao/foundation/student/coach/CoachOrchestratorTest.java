package com.zhijiao.foundation.student.coach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.knowledge.KnowledgeQueryPort;
import com.zhijiao.foundation.knowledge.KnowledgeSearchResult;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import com.zhijiao.foundation.student.learning.LearningStateView;
import com.zhijiao.foundation.student.learning.StudentAbilityState;
import com.zhijiao.foundation.student.learning.StudentKnowledgeState;
import com.zhijiao.foundation.student.learning.WeakKnowledgePointCandidate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class CoachOrchestratorTest {

    @Test
    void invalidDiagnosticOutputIsRetriedAndValidOutputProducesTwoQuestions() {
        LearningStateEngine learningStateEngine = mock(LearningStateEngine.class);
        KnowledgeQueryPort knowledgeQueryPort = mock(KnowledgeQueryPort.class);
        CoachRepository repository = mock(CoachRepository.class);
        LlmPort llmPort = mock(LlmPort.class);
        when(learningStateEngine.read("stu-xiaoming", "course-data-structures", "kp-graph-bfs-dfs"))
                .thenReturn(view());
        when(repository.findSession("session-1")).thenReturn(Optional.of(new CoachSession(
                "session-1", "stu-xiaoming", "course-data-structures", "kp-graph-bfs-dfs", "DIAGNOSTIC",
                "ACTIVE", RagStatus.EMPTY, 0.17, 0.74, 0.11, 0.71, "LOW_MASTERY",
                "BKT_V1_FIXED_PARAMS/RASCH_MAP_V1", "baseline-ds-v1", Instant.now(), Instant.now())));
        when(knowledgeQueryPort.search(any(), any(), any(), anyInt())).thenReturn(List.of(
                new KnowledgeSearchResult("chunk-1", "doc-1", "讲义", "BFS 使用队列。", 0.9, List.of())));
        when(llmPort.complete(any()))
                .thenReturn(new LlmResponse("not json", "test", "test", "test"))
                .thenReturn(new LlmResponse(validJson(), "test", "test", "test"));
        when(repository.saveDiagnosticSet(any(), any(), any(), any(), any(), any(), any())).thenAnswer(invocation ->
                invocation.getArgument(0));

        CoachOrchestrator orchestrator = new CoachOrchestrator(
                learningStateEngine, knowledgeQueryPort, llmPort, repository,
                new ObjectMapper(), new DiagnosticQuestionValidator(), 1, "coach-prompt-v1");

        DiagnosticSetResult result = orchestrator.generateDiagnosticSet(
                "session-1", "stu-xiaoming", "course-data-structures", "kp-graph-bfs-dfs");

        assertThat(result.questions()).hasSize(2);
        assertThat(result.ragStatus()).isEqualTo(RagStatus.INDEXED);
    }

    @Test
    void ragFailureProducesDegradedResponseWithoutFakeCitation() {
        LearningStateEngine learningStateEngine = mock(LearningStateEngine.class);
        KnowledgeQueryPort knowledgeQueryPort = mock(KnowledgeQueryPort.class);
        CoachRepository repository = mock(CoachRepository.class);
        LlmPort llmPort = mock(LlmPort.class);
        Instant now = Instant.parse("2026-08-29T23:59:59Z");
        when(repository.findSession("session-2")).thenReturn(Optional.of(new CoachSession(
                "session-2", "stu-xiaoming", "course-data-structures", "kp-graph-bfs-dfs", "TUTOR", "ACTIVE",
                RagStatus.EMPTY, 0.17, 0.74, 0.11, 0.71, "LOW_MASTERY",
                "BKT_V1_FIXED_PARAMS/RASCH_MAP_V1", "baseline-ds-v1", now, now)));
        doThrow(new com.zhijiao.foundation.knowledge.KnowledgeUnavailableException("down", null))
                .when(knowledgeQueryPort).search(any(), any(), any(), anyInt());
        when(llmPort.complete(any())).thenReturn(new LlmResponse("仅基于对话回答。", "test", "test", "test"));

        CoachOrchestrator orchestrator = new CoachOrchestrator(learningStateEngine, knowledgeQueryPort, llmPort,
                repository, new ObjectMapper(), new DiagnosticQuestionValidator(), 0, "coach-prompt-v1");
        CoachMessageResult result = orchestrator.sendMessage("session-2", "解释 BFS");

        assertThat(result.ragStatus()).isEqualTo(RagStatus.DEGRADED);
        assertThat(result.citations()).isEmpty();
    }

    private String validJson() {
        return """
                {"questions":[
                  {"questionId":"q-1","knowledgePointId":"kp-graph-bfs-dfs","questionType":"SINGLE_CHOICE","stem":"BFS uses which structure?","options":[{"optionId":"A","text":"队列"},{"optionId":"B","text":"栈"}],"correctAnswer":"A","explanation":"按层访问需要队列。","diagnosticTarget":{"code":"BFS_QUEUE_ORDER","description":"访问顺序"},"difficulty":0.5},
                  {"questionId":"q-2","knowledgePointId":"kp-graph-bfs-dfs","questionType":"SINGLE_CHOICE","stem":"DFS returns when?","options":[{"optionId":"A","text":"回溯"},{"optionId":"B","text":"永不返回"}],"correctAnswer":"A","explanation":"递归会回溯。","diagnosticTarget":{"code":"DFS_BACKTRACKING","description":"回溯"},"difficulty":0.6}
                ]}
                """;
    }

    private LearningStateView view() {
        Instant now = Instant.parse("2026-08-29T23:59:59Z");
        StudentKnowledgeState state = new StudentKnowledgeState("stu-xiaoming", "course-data-structures",
                "class-cs-2024-01", "kp-graph-bfs-dfs", "图遍历 BFS / DFS", 0.17, 0.74, 0.11,
                5, now, "BKT_V1_FIXED_PARAMS", "RASCH_MAP_V1", "RECENCY_GAP_V1",
                "STATE_CONFIDENCE_V1", now, "baseline-ds-v1");
        StudentAbilityState ability = new StudentAbilityState("stu-xiaoming", "course-data-structures",
                0.18, 0.6, "RASCH_MAP_V1", now, "baseline-ds-v1");
        WeakKnowledgePointCandidate candidate = new WeakKnowledgePointCandidate("stu-xiaoming",
                "course-data-structures", "kp-graph-bfs-dfs", "图遍历 BFS / DFS", 0.71, 0.74, 5, 1,
                List.of("LOW_MASTERY"), "WEAK_RANKING_V1");
        return new LearningStateView(state, ability, List.of(candidate));
    }
}
