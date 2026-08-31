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
                .andExpect(jsonPath("$.data.assets[*].assetKey").value(org.hamcrest.Matchers.hasItems("student-risk", "intervention-outcome", "aichat")));
    }

    @Test
    void returnsVerifiedStudentRiskAssetFromDefaultConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/student-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetKey").value("student-risk"))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.launchMode").value("IFRAME"))
                .andExpect(jsonPath("$.data.resourceUrl").value("https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/openresource.jsp?resid=d552d9327e2668555c353b0992a28ae9"));
    }

    @Test
    void returnsVerifiedInterventionOutcomeAssetFromDefaultConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/intervention-outcome"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetKey").value("intervention-outcome"))
                .andExpect(jsonPath("$.data.displayName").value("干预成效分析"))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.launchMode").value("IFRAME"))
                .andExpect(jsonPath("$.data.resourceUrl").value("https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/openresource.jsp?resid=5f8c6324154b11ca6d4c5c02b18a1b34"));
    }

    @Test
    void returnsVerifiedAiChatAssetFromDefaultConfiguration() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/aichat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetKey").value("aichat"))
                .andExpect(jsonPath("$.data.type").value("AICHAT"))
                .andExpect(jsonPath("$.data.displayName").value("AI 分析助手"))
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.launchMode").value("IFRAME"))
                .andExpect(jsonPath("$.data.resourceUrl").value("https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/aichat/proxy/#/canvas/chat"));
    }

    @Test
    void unknownAssetUsesUnifiedNotFoundEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/integrations/smartbi/assets/not-a-real-asset"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("SmartBI asset")));
    }
}
