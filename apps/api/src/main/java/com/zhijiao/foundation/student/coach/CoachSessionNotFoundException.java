package com.zhijiao.foundation.student.coach;

public class CoachSessionNotFoundException extends RuntimeException {
    public CoachSessionNotFoundException(String sessionId) {
        super("Coach session not found: " + sessionId);
    }
}
