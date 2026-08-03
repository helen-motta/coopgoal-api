package com.coopgoal.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProposalVotingIntegrationTest extends AbstractIntegrationTest {
    @Test
    void completesProposalVotingFlowAndCancelsGoal() throws Exception {
        String ownerToken = register("Ana", "ana@example.com");
        String adminToken = register("Bruno", "bruno@example.com");
        register("Carla", "carla@example.com");

        String groupId = json(mvc.perform(post("/api/groups").header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Evento\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();
        addMember(groupId, ownerToken, "bruno@example.com", "ADMIN");
        addMember(groupId, ownerToken, "carla@example.com", "MEMBER");

        String goalBody = """
                {"name":"Local","targetAmount":5000.00,"deadline":"%s"}
                """.formatted(LocalDate.now().plusMonths(4));
        String goalId = json(mvc.perform(post("/api/groups/{id}/goals", groupId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(goalBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();

        String proposalBody = """
                {"type":"CANCEL_GOAL","justification":"Evento cancelado","expiresAt":"%s"}
                """.formatted(Instant.now().plusSeconds(3600));
        String proposalId = json(mvc.perform(post("/api/goals/{id}/proposals", goalId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(proposalBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asText();

        vote(proposalId, ownerToken);
        vote(proposalId, adminToken);

        mvc.perform(get("/api/proposals/{id}", proposalId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"));
        mvc.perform(get("/api/goals/{id}", goalId).header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private void addMember(String groupId, String token, String email, String role) throws Exception {
        mvc.perform(post("/api/groups/{id}/members", groupId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"role\":\"%s\"}".formatted(email, role)))
                .andExpect(status().isCreated());
    }

    private void vote(String proposalId, String token) throws Exception {
        mvc.perform(post("/api/proposals/{id}/votes", proposalId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"choice\":\"APPROVE\"}"))
                .andExpect(status().isCreated());
    }
}
