package com.zhijiao.foundation.student.coach;

import com.zhijiao.foundation.knowledge.Citation;

import java.util.List;

public record DiagnosticQuestion(
        String questionId,
        String knowledgePointId,
        String questionType,
        String stem,
        List<QuestionOption> options,
        String correctAnswer,
        String explanation,
        DiagnosticTarget diagnosticTarget,
        double difficulty,
        List<Citation> citations,
        String modelProvider,
        String modelVersion,
        String promptVersion
) {
}
