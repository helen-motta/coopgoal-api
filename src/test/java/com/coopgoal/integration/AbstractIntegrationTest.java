package com.coopgoal.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
abstract class AbstractIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE audit_logs, votes, proposals, recurring_contributions, " +
                "contributions, financial_goals, memberships, coop_groups, users CASCADE");
    }

    String register(String name, String email) throws Exception {
        String body = """
                {"name":"%s","email":"%s","password":"SenhaForte@123"}
                """.formatted(name, email);
        String response = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    JsonNode json(String response) throws Exception { return objectMapper.readTree(response); }
}
