package com.zhijiao.foundation.student.practice;

public class PracticeAttemptNotFoundException extends RuntimeException {
    public PracticeAttemptNotFoundException(String attemptId) {
        super("Practice attempt not found: " + attemptId);
    }
}
