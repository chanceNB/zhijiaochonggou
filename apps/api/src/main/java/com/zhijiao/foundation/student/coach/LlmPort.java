package com.zhijiao.foundation.student.coach;

public interface LlmPort {
    LlmResponse complete(LlmRequest request);
}
