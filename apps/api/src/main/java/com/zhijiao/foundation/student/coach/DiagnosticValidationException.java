package com.zhijiao.foundation.student.coach;

public class DiagnosticValidationException extends RuntimeException {
    public DiagnosticValidationException(String message) {
        super(message);
    }

    public DiagnosticValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
