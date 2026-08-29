package com.zhijiao.foundation.demo;

public class DemoRunNotFoundException extends RuntimeException {
    public DemoRunNotFoundException(String demoRunId) {
        super("Demo run not found: " + demoRunId);
    }
}
