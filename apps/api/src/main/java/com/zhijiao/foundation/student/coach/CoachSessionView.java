package com.zhijiao.foundation.student.coach;

import java.util.List;

public record CoachSessionView(CoachSession session, List<CoachMessage> messages,
                               List<DiagnosticQuestion> diagnosticQuestions) {
}
