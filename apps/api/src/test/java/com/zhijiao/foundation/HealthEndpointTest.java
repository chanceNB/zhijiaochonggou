package com.zhijiao.foundation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthIsExposedAsUnifiedEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/health").header("X-Request-Id", "req-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.requestId", equalTo("req-health")))
                .andExpect(jsonPath("$.data.status", equalTo("UP")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
