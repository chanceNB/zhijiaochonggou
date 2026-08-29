package com.zhijiao.foundation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayFoundationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void foundationSchemasAreCreatedByFlyway() {
        Integer app = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.schemata where schema_name = 'APP'", Integer.class);
        Integer exchange = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.schemata where schema_name = 'SMARTBI_EXCHANGE'", Integer.class);

        assertThat(app).isEqualTo(1);
        assertThat(exchange).isEqualTo(1);
    }
}
