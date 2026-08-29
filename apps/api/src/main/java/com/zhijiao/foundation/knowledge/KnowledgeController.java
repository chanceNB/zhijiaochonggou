package com.zhijiao.foundation.knowledge;

import com.zhijiao.foundation.api.ApiEnvelope;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/knowledge")
public class KnowledgeController {
    private final KnowledgeIngestionService ingestionService;
    private final KnowledgeRepository repository;
    private final Clock clock;

    public KnowledgeController(KnowledgeIngestionService ingestionService, KnowledgeRepository repository, Clock clock) {
        this.ingestionService = ingestionService;
        this.repository = repository;
        this.clock = clock;
    }

    @PostMapping(value = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiEnvelope<DocumentResponse> ingest(@Valid @RequestBody DocumentRequest request,
                                                @RequestHeader(name = "Idempotency-Key") String ignored,
                                                HttpServletRequest servletRequest) {
        KnowledgeDocument document = ingestionService.ingestText(request.courseId(), request.title(), request.source(),
                request.content(), request.knowledgePointId(), request.sourceVersion(), request.dataOrigin());
        return success(servletRequest, new DocumentResponse(document.documentId(), document.title(), document.status(),
                repository.countChunks(document.documentId())));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiEnvelope<DocumentResponse> ingestMultipart(
            @RequestParam String courseId,
            @RequestParam(required = false) String knowledgePointId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String sourceVersion,
            @RequestParam(required = false) String dataOrigin,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(name = "Idempotency-Key") String ignored,
            HttpServletRequest servletRequest) throws java.io.IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file must not be empty");
        }
        String resolvedTitle = title == null || title.isBlank() ? file.getOriginalFilename() : title;
        KnowledgeDocument document = ingestionService.ingestText(courseId, resolvedTitle,
                file.getOriginalFilename(), new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8),
                knowledgePointId, sourceVersion, dataOrigin);
        return success(servletRequest, new DocumentResponse(document.documentId(), document.title(), document.status(),
                repository.countChunks(document.documentId())));
    }

    @GetMapping("/documents")
    public ApiEnvelope<DocumentListResponse> list(@RequestParam String courseId, HttpServletRequest servletRequest) {
        List<DocumentResponse> items = repository.findDocuments(courseId).stream()
                .map(document -> new DocumentResponse(document.documentId(), document.title(), document.status(),
                        repository.countChunks(document.documentId())))
                .toList();
        return success(servletRequest, new DocumentListResponse(items));
    }

    private <T> ApiEnvelope<T> success(HttpServletRequest request, T data) {
        String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return ApiEnvelope.success(requestId, data, Instant.now(clock));
    }

    public record DocumentRequest(@NotBlank String courseId, @NotBlank String title, String source,
                                  @NotBlank String content, String knowledgePointId,
                                  String sourceVersion, String dataOrigin) {
    }

    public record DocumentResponse(String documentId, String title, String status, int chunkCount) {
    }

    public record DocumentListResponse(List<DocumentResponse> items) {
    }
}
