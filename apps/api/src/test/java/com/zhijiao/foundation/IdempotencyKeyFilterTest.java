package com.zhijiao.foundation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.web.IdempotencyKeyFilter;
import com.zhijiao.foundation.web.RequestIdFilter;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyKeyFilterTest {

    @Test
    void missingKeyReturnsUnifiedValidationEnvelope() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/future-command");
        request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, "req-test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        new IdempotencyKeyFilter(objectMapper).doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("\"code\":\"VALIDATION_ERROR\"");
        assertThat(response.getContentAsString()).contains("\"requestId\":\"req-test\"");
    }
}
