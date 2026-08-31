package com.zhijiao.foundation.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "smartbi.assets.student-risk.status=PLATFORM_PENDING",
        "smartbi.assets.student-risk.launch-mode=UNVERIFIED",
        "smartbi.assets.student-risk.resource-url=",
        "smartbi.assets.aichat.status=PLATFORM_PENDING",
        "smartbi.assets.aichat.launch-mode=UNVERIFIED",
        "smartbi.assets.aichat.resource-url="
})
@AutoConfigureMockMvc
class SmartBiAssetPendingIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void preservesPendingStateWhenRuntimeConfigurationIsNotVerified() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/student-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PLATFORM_PENDING"))
                .andExpect(jsonPath("$.data.launchMode").value("UNVERIFIED"))
                .andExpect(jsonPath("$.data.resourceUrl").doesNotExist());
    }

    @Test
    void preservesPendingAiChatStateWhenRuntimeConfigurationIsNotVerified() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/aichat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PLATFORM_PENDING"))
                .andExpect(jsonPath("$.data.launchMode").value("UNVERIFIED"))
                .andExpect(jsonPath("$.data.resourceUrl").doesNotExist());
    }
}
