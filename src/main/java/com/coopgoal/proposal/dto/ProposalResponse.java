package com.coopgoal.proposal.dto;

import com.coopgoal.proposal.domain.Proposal;
import com.coopgoal.proposal.domain.ProposalStatus;
import com.coopgoal.proposal.domain.ProposalType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProposalResponse(UUID id, UUID goalId, UUID createdBy, String createdByName,
                               ProposalType type, String proposedValue, String justification,
                               ProposalStatus status, Instant expiresAt, Instant createdAt,
                               List<VoteResponse> votes) {
    public static ProposalResponse from(Proposal proposal, List<VoteResponse> votes) {
        return new ProposalResponse(proposal.getId(), proposal.getGoal().getId(),
                proposal.getCreatedBy().getId(), proposal.getCreatedBy().getName(), proposal.getType(),
                proposal.getProposedValue(), proposal.getJustification(), proposal.getStatus(),
                proposal.getExpiresAt(), proposal.getCreatedAt(), votes);
    }
}
