package com.zhijiao.foundation.student.practice;

public record PracticeAttemptResult(String attemptId, boolean correct, String correctAnswer,
                                    String explanation, String misconceptionCode,
                                    boolean canAddWrongBook, boolean canGenerateSimilar) {
}
