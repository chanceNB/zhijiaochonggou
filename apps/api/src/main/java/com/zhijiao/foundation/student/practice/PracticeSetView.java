package com.zhijiao.foundation.student.practice;

import java.util.List;

public record PracticeSetView(PracticeSet practiceSet, List<StudentQuestion> questions, List<PracticeAttemptSummary> attempts) {
}
