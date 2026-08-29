package com.zhijiao.foundation.student.coach;

import com.zhijiao.foundation.knowledge.Citation;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public final class DiagnosticQuestionValidator {
    public void validate(List<DiagnosticQuestion> questions, String knowledgePointId, int expectedCount) {
        if (questions == null || questions.size() != expectedCount) {
            throw new DiagnosticValidationException("Expected exactly " + expectedCount + " diagnostic questions");
        }
        Set<String> questionIds = new HashSet<>();
        for (DiagnosticQuestion question : questions) {
            if (question == null || blank(question.questionId()) || !questionIds.add(question.questionId())
                    || !knowledgePointId.equals(question.knowledgePointId())) {
                throw new DiagnosticValidationException("Question identity or knowledge point is invalid");
            }
            if (!"SINGLE_CHOICE".equals(question.questionType()) || blank(question.stem())
                    || blank(question.explanation()) || question.options() == null || question.options().size() < 2
                    || blank(question.correctAnswer()) || question.diagnosticTarget() == null
                    || blank(question.diagnosticTarget().code()) || blank(question.diagnosticTarget().description())
                    || !Double.isFinite(question.difficulty()) || question.difficulty() < 0.0 || question.difficulty() > 1.0) {
                throw new DiagnosticValidationException("Question required fields are invalid");
            }
            Set<String> optionIds = new HashSet<>();
            for (QuestionOption option : question.options()) {
                if (option == null || blank(option.optionId()) || blank(option.text()) || !optionIds.add(option.optionId())) {
                    throw new DiagnosticValidationException("Question options must be unique and non-empty");
                }
            }
            if (!optionIds.contains(question.correctAnswer())) {
                throw new DiagnosticValidationException("Correct answer must match an option");
            }
            if (question.citations() != null) {
                for (Citation citation : question.citations()) {
                    if (citation == null || blank(citation.documentId()) || blank(citation.chunkId())
                            || blank(citation.title()) || blank(citation.excerpt())
                            || !Double.isFinite(citation.score()) || citation.score() < 0.0 || citation.score() > 1.0) {
                        throw new DiagnosticValidationException("Citation structure is invalid");
                    }
                }
            }
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
