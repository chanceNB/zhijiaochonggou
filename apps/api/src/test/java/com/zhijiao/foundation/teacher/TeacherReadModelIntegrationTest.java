package com.zhijiao.foundation.teacher;

import com.zhijiao.foundation.demo.BaselineSeedService;
import com.zhijiao.foundation.demo.DemoRunService;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherReadModelIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired BaselineSeedService baselineSeedService;
    @Autowired LearningStateEngine learningStateEngine;
    @Autowired DemoRunService demoRunService;

    @BeforeEach
    void ensureBaseline() {
        baselineSeedService.seed();
        learningStateEngine.recompute(BaselineSeedService.BASELINE_VERSION);
        demoRunService.create(BaselineSeedService.DEMO_CASE_ID, BaselineSeedService.BASELINE_VERSION);
    }

    @Test
    void workbenchResolvesCurrentStudentWithoutCohortDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/workbench"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("OK")))
                .andExpect(jsonPath("$.data.currentStudent.displayName", equalTo("小明")))
                .andExpect(jsonPath("$.data.currentStudent.courseName", equalTo("数据结构")))
                .andExpect(jsonPath("$.data.priorityItems", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.classCount").doesNotExist());
    }

    @Test
    void profileReturnsRealAttemptsAndUnknownStudentIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/students/stu-xiaoming/profile")
                        .param("courseId", "course-data-structures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.student.displayName", equalTo("小明")))
                .andExpect(jsonPath("$.data.recentAttempts", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.learningState.knowledgePointName").isString());

        mockMvc.perform(get("/api/v1/teacher/students/missing-student/profile")
                        .param("courseId", "course-data-structures"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("RESOURCE_NOT_FOUND")));
    }

    @Test
    void diagnosisReturnsEvidenceForActiveCaseAndUnknownCaseIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/teacher/diagnosis-cases/DEMO-GRAPH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.caseId", equalTo("DEMO-GRAPH-001")))
                .andExpect(jsonPath("$.data.primaryHypothesis").isString())
                .andExpect(jsonPath("$.data.evidence", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.confidence").isNumber());

        mockMvc.perform(get("/api/v1/teacher/diagnosis-cases/missing-case"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", equalTo("RESOURCE_NOT_FOUND")));
    }
}
