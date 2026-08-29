package com.zhijiao.foundation;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiContractTest {

    @Test
    void foundationOpenApiIsParseable() throws Exception {
        Path contract = Path.of("contracts", "openapi.yaml");
        if (!Files.exists(contract)) {
            contract = Path.of("..", "..", "contracts", "openapi.yaml").normalize();
        }
        try (InputStream input = Files.newInputStream(contract)) {
            Map<String, Object> document = new Yaml().load(input);
            assertThat(document).containsEntry("openapi", "3.0.3");
            assertThat(document).containsKey("paths");
            @SuppressWarnings("unchecked")
            Map<String, Object> paths = (Map<String, Object>) document.get("paths");
            assertThat(paths).containsKeys(
                    "/demo/runs",
                    "/demo/runs/{demoRunId}",
                    "/demo/runs/{demoRunId}/reset",
                    "/analytics/demo-traces/{demoCaseId}",
                    "/analytics/smartbi/datasets",
                    "/analytics/smartbi/freshness",
                    "/analytics/smartbi/exports",
                    "/analytics/smartbi/exports/{exportId}",
                    "/student/learning-state",
                    "/student/growth",
                    "/student/coach/sessions",
                    "/student/coach/sessions/{sessionId}",
                    "/student/coach/sessions/{sessionId}/messages",
                    "/student/coach/sessions/{sessionId}/diagnostic-sets",
                    "/student/coach/sessions/{sessionId}/similar-questions",
                    "/student/practice-sets/{practiceSetId}",
                    "/student/practice-sets/{practiceSetId}/attempts",
                    "/student/practice-sets/{practiceSetId}/complete",
                    "/student/practice-attempts/{attemptId}/wrong-book",
                    "/student/wrong-book",
                    "/student/wrong-book/{wrongItemId}/review",
                    "/admin/knowledge/documents");
            @SuppressWarnings("unchecked")
            Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) document.get("components")).get("schemas");
            assertThat(schemas).containsKeys("StudentQuestion", "PracticeSetResponse", "PracticeAttemptResponse",
                    "PracticeOutcome", "WrongBookPage", "WrongBookReviewResponse", "ApiEnvelopePracticeSetResponse");
            assertThat((Map<String, Object>) schemas.get("StudentQuestion")).doesNotContainKey("correctAnswer");
            assertThat((Map<String, Object>) schemas.get("StudentQuestion")).doesNotContainKey("explanation");
        }
    }
}
