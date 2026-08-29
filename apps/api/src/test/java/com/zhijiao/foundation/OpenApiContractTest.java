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
                    "/student/learning-state",
                    "/student/growth");
        }
    }
}
