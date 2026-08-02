package com.coopgoal.proposal.dto;

import com.coopgoal.proposal.domain.Vote;
import com.coopgoal.proposal.domain.VoteChoice;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(UUID id, UUID userId, String userName, VoteChoice choice, Instant createdAt) {
    public static VoteResponse from(Vote vote) {
        return new VoteResponse(vote.getId(), vote.getUser().getId(), vote.getUser().getName(),
                vote.getChoice(), vote.getCreatedAt());
    }
}
