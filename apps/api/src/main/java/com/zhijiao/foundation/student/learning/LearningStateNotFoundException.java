package com.zhijiao.foundation.student.learning;

public class LearningStateNotFoundException extends RuntimeException {
    public LearningStateNotFoundException(String studentId, String courseId, String knowledgePointId) {
        super("Learning state not found for student " + studentId + ", course " + courseId
                + (knowledgePointId == null ? "" : ", knowledge point " + knowledgePointId));
    }
}
