package com.zhijiao.foundation.student.practice;

public class PracticeSetNotFoundException extends RuntimeException {
    public PracticeSetNotFoundException(String practiceSetId) {
        super("Practice set not found: " + practiceSetId);
    }
}
