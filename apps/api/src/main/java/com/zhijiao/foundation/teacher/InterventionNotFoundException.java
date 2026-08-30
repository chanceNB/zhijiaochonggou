package com.zhijiao.foundation.teacher;

public class InterventionNotFoundException extends RuntimeException {
    public InterventionNotFoundException(String interventionId) {
        super("Intervention not found: " + interventionId);
    }
}
