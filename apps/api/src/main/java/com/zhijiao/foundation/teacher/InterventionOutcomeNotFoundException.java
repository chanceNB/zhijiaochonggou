package com.zhijiao.foundation.teacher;

public class InterventionOutcomeNotFoundException extends RuntimeException {
    public InterventionOutcomeNotFoundException(String interventionId) {
        super("Intervention outcome was not found: " + interventionId);
    }
}
