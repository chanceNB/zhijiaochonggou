package com.zhijiao.foundation.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalyticsExportService {
    private static final List<String> EXPORT_DATASETS = List.of(
            "sb_dim_course", "sb_dim_class", "sb_dim_student", "sb_dim_knowledge_point",
            "sb_fact_learning_state", "sb_fact_practice_attempt", "sb_fact_wrong_book",
            "sb_fact_diagnosis", "sb_fact_analysis_recommendation", "sb_fact_analysis_recommendation_candidate",
            "sb_fact_intervention", "sb_fact_intervention_assignment", "sb_fact_intervention_outcome");

    private final JdbcTemplate jdbcTemplate;
    private final AnalyticsProjectionService projectionService;
    private final Clock clock;
    private final Map<String, AnalyticsExport> idempotentExports = new ConcurrentHashMap<>();
    private final Map<String, AnalyticsExport> exports = new ConcurrentHashMap<>();

    public AnalyticsExportService(JdbcTemplate jdbcTemplate, AnalyticsProjectionService projectionService, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectionService = projectionService;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public AnalyticsExport create(String scope, String demoRunId, String idempotencyKey) {
        String normalizedScope = scope == null || scope.isBlank() ? "ACTIVE_DEMO" : scope.trim().toUpperCase();
        if (normalizedScope.equals("CURRENT_ALL")) normalizedScope = "ALL";
        if (!normalizedScope.equals("ACTIVE_DEMO") && !normalizedScope.equals("ALL")) {
            throw new IllegalArgumentException("scope must be ACTIVE_DEMO or CURRENT_ALL");
        }
        if (idempotencyKey != null) {
            AnalyticsExport existing = idempotentExports.get(idempotencyKey);
            if (existing != null) return existing;
        }
        projectionService.refresh();
        Instant createdAt = Instant.now(clock);
        String exportId = "analytics-export-" + UUID.randomUUID().toString().replace("-", "");
        try {
            Path directory = Files.createTempDirectory("zhijiao-analytics-" + exportId + "-");
            List<String> files = new ArrayList<>();
            for (String dataset : EXPORT_DATASETS) {
                Path file = directory.resolve(dataset + ".csv");
                writeCsv(dataset, file, normalizedScope, demoRunId);
                files.add(file.toString());
            }
            Path manifest = directory.resolve("manifest.json");
            writeManifest(manifest, exportId, normalizedScope, demoRunId, files, createdAt);
            List<String> allFiles = new ArrayList<>(files);
            allFiles.add(manifest.toString());
            AnalyticsExport result = new AnalyticsExport(exportId, "SUCCEEDED", normalizedScope, demoRunId,
                    createdAt, Instant.now(clock), List.copyOf(allFiles), manifest.toString());
            exports.put(exportId, result);
            if (idempotencyKey != null) idempotentExports.putIfAbsent(idempotencyKey, result);
            return idempotencyKey == null ? result : idempotentExports.get(idempotencyKey);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create analytics CSV export", exception);
        }
    }

    public AnalyticsExport get(String exportId) {
        AnalyticsExport result = exports.get(exportId);
        if (result == null) throw new AnalyticsExportNotFoundException(exportId);
        return result;
    }

    private void writeCsv(String dataset, Path file, String scope, String demoRunId) throws IOException {
        String table = "smartbi_exchange." + dataset;
        String sql = "select * from " + table;
        if (scope.equals("ACTIVE_DEMO")) {
            if (dataset.equals("sb_fact_learning_state")) {
                sql += " where data_origin = 'BASELINE_SIMULATED' or exists (select 1 from app.demo_runs d where d.demo_run_id = "
                        + table + ".demo_run_id and d.status = 'ACTIVE')";
            } else if (dataset.equals("sb_fact_practice_attempt") || dataset.equals("sb_fact_wrong_book")) {
                sql += " where data_origin = 'BASELINE_SIMULATED' or is_active_demo = true";
            } else if (dataset.equals("sb_demo_run_state")) {
                sql += " where active = true";
            } else if (dataset.equals("sb_fact_analysis_recommendation")
                    || dataset.equals("sb_fact_analysis_recommendation_candidate")
                    || dataset.equals("sb_fact_intervention")
                    || dataset.equals("sb_fact_intervention_assignment")
                    || dataset.equals("sb_fact_intervention_outcome")) {
                sql += " where is_active_demo = true";
            }
        }
        if (demoRunId != null && !demoRunId.isBlank()
                && (dataset.startsWith("sb_fact_") || dataset.equals("sb_demo_run_state"))) {
            sql += (sql.contains(" where ") ? " and " : " where ") + "demo_run_id = ?";
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            jdbcTemplate.query(sql, ps -> {
                if (demoRunId != null && !demoRunId.isBlank()
                        && (dataset.startsWith("sb_fact_") || dataset.equals("sb_demo_run_state"))) {
                    ps.setString(1, demoRunId);
                }
            }, rs -> {
                try {
                    int columns = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= columns; i++) {
                        if (i > 1) writer.write(',');
                        writer.write(escape(rs.getMetaData().getColumnName(i)));
                    }
                    writer.newLine();
                    while (rs.next()) {
                        for (int i = 1; i <= columns; i++) {
                            if (i > 1) writer.write(',');
                            Object value = rs.getObject(i);
                            writer.write(escape(value == null ? "" : String.valueOf(value)));
                        }
                        writer.newLine();
                    }
                    return null;
                } catch (IOException exception) {
                    throw new CsvWriteRuntimeException(exception);
                }
            });
        } catch (CsvWriteRuntimeException exception) {
            throw exception.ioException;
        }
    }

    private void writeManifest(Path file, String exportId, String scope, String demoRunId,
                               List<String> files, Instant createdAt) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("{\"exportId\":\"" + escapeJson(exportId) + "\",\"scope\":\"" + escapeJson(scope)
                    + "\",\"demoRunId\":" + (demoRunId == null ? "null" : "\"" + escapeJson(demoRunId) + "\"")
                    + ",\"createdAt\":\"" + createdAt + "\",\"files\":[");
            for (int i = 0; i < files.size(); i++) {
                if (i > 0) writer.write(',');
                writer.write("\"" + escapeJson(files.get(i)) + "\"");
            }
            writer.write("]}");
        }
    }

    private String escape(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class CsvWriteRuntimeException extends RuntimeException {
        private final IOException ioException;

        private CsvWriteRuntimeException(IOException ioException) {
            this.ioException = ioException;
        }
    }
}
