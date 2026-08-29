package com.zhijiao.foundation.student.coach;

public class LlmTimeoutException extends RuntimeException {
    public LlmTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
