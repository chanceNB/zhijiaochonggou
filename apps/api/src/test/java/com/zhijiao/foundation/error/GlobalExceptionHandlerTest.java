package com.zhijiao.foundation.error;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.zhijiao.foundation.web.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void unexpectedExceptionIsLoggedWithRequestContextAndStackTrace() {
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/student/coach/sessions/diagnostic-sets");
            request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, "req-diagnostic");
            RuntimeException exception = new RuntimeException("diagnostic root cause");

            ResponseEntity<?> response = new GlobalExceptionHandler().unexpected(exception, request);

            assertThat(response.getStatusCode().value()).isEqualTo(500);
            assertThat(response.getBody()).extracting("code", "message", "requestId")
                    .containsExactly("INTERNAL_ERROR", "Service temporarily unavailable", "req-diagnostic");
            ILoggingEvent event = appender.list.stream()
                    .filter(item -> item.getFormattedMessage().contains("req-diagnostic"))
                    .findFirst()
                    .orElseThrow();
            assertThat(event.getFormattedMessage())
                    .contains("uri=/api/v1/student/coach/sessions/diagnostic-sets")
                    .contains("exceptionClass=java.lang.RuntimeException")
                    .contains("exceptionMessage=diagnostic root cause");
            assertThat(event.getThrowableProxy()).isNotNull();
            assertThat(event.getThrowableProxy().getClassName()).isEqualTo(RuntimeException.class.getName());
            assertThat(event.getThrowableProxy().getMessage()).isEqualTo("diagnostic root cause");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
