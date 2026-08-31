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
        "smartbi.assets.student-risk.status=VERIFIED",
        "smartbi.assets.student-risk.launch-mode=IFRAME",
        "smartbi.assets.student-risk.resource-url=https://smartbi.example.test/student-risk",
        "smartbi.assets.intervention-outcome.status=VERIFIED",
        "smartbi.assets.intervention-outcome.launch-mode=NEW_TAB",
        "smartbi.assets.intervention-outcome.resource-url=https://smartbi.example.test/intervention-outcome"
})
@AutoConfigureMockMvc
class SmartBiAssetConfigurationIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void returnsVerifiedIframeAssetFromBackendConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/student-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.launchMode").value("IFRAME"))
                .andExpect(jsonPath("$.data.resourceUrl").value("https://smartbi.example.test/student-risk"));
    }

    @Test
    void returnsVerifiedNewTabAssetFromBackendConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/intervention-outcome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.launchMode").value("NEW_TAB"))
                .andExpect(jsonPath("$.data.resourceUrl").value("https://smartbi.example.test/intervention-outcome"));
    }
}
