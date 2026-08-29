package com.zhijiao.foundation.student.practice;

public class WrongBookItemNotFoundException extends RuntimeException {
    public WrongBookItemNotFoundException(String wrongItemId) {
        super("Wrong-book item not found: " + wrongItemId);
    }
}
