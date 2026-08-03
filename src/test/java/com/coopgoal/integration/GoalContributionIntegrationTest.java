package com.coopgoal.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GoalContributionIntegrationTest extends AbstractIntegrationTest {
    @Test
    void createsQueriesAndCompletesGoalWithContribution() throws Exception {
        String token = register("Ana Silva", "ana@example.com");
        String groupResponse = mvc.perform(post("/api/groups").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Viagem\",\"description\":\"Férias\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String groupId = json(groupResponse).get("id").asText();

        String goalBody = """
                {"name":"Passagens","targetAmount":100.00,"deadline":"%s"}
                """.formatted(LocalDate.now().plusMonths(3));
        String goalResponse = mvc.perform(post("/api/groups/{id}/goals", groupId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(goalBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        JsonNode goal = json(goalResponse);
        String goalId = goal.get("id").asText();

        mvc.perform(get("/api/groups/{id}/goals", groupId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Passagens"));

        mvc.perform(post("/api/goals/{id}/contributions", goalId)
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "integration-contribution-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.00,\"description\":\"Pagamento\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/goals/{id}/progress", goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contributedAmount").value(100.0))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
