package com.coopgoal.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends AbstractIntegrationTest {
    @Test
    void registersPersistsAndAuthenticatesUser() throws Exception {
        register("Ana Silva", "ana@example.com");
        assertThat(jdbc.queryForObject("select count(*) from users where email = 'ana@example.com'", Long.class))
                .isEqualTo(1L);

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"SenhaForte@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void rejectsInvalidCredentialsWithUnauthorized() throws Exception {
        register("Ana Silva", "ana@example.com");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"password\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized());
    }
}
