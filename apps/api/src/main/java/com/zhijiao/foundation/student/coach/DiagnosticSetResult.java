package com.zhijiao.foundation.student.coach;

import java.util.List;

public record DiagnosticSetResult(String practiceSetId, List<DiagnosticQuestion> questions, RagStatus ragStatus) {
}
