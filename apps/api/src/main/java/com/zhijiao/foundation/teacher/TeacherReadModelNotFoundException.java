package com.zhijiao.foundation.teacher;

public class TeacherReadModelNotFoundException extends RuntimeException {
    public TeacherReadModelNotFoundException(String resource, String id) {
        super(resource + " not found: " + id);
    }
}
