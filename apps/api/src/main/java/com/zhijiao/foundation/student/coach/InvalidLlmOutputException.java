package com.zhijiao.foundation.student.coach;

public class InvalidLlmOutputException extends RuntimeException {
    public InvalidLlmOutputException(String message, Throwable cause) {
        super(message, cause);
    }
}
