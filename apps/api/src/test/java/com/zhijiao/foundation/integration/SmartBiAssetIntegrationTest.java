package com.zhijiao.foundation.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SmartBiAssetIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void listsContractDefinedSmartBiAssets() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.assets[*].assetKey").value(org.hamcrest.Matchers.hasItems("student-risk", "intervention-outcome")));
    }

    @Test
    void returnsPendingAssetWithoutInventedUrlWhenPlatformIsUnverified() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/student-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetKey").value("student-risk"))
                .andExpect(jsonPath("$.data.status").value("PLATFORM_PENDING"))
                .andExpect(jsonPath("$.data.launchMode").value("UNVERIFIED"))
                .andExpect(jsonPath("$.data.resourceUrl").doesNotExist());
    }

    @Test
    void unknownAssetUsesUnifiedNotFoundEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/not-a-real-asset"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("SmartBI asset")));
    }
}
