package com.coopgoal.contribution.dto;

import com.coopgoal.contribution.domain.Contribution;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContributionResponse(UUID id, UUID goalId, UUID memberId, UUID userId, String userName,
                                   BigDecimal amount, String description, Instant createdAt) {
    public static ContributionResponse from(Contribution contribution) {
        return new ContributionResponse(contribution.getId(), contribution.getGoal().getId(),
                contribution.getMember().getId(), contribution.getMember().getUser().getId(),
                contribution.getMember().getUser().getName(), contribution.getAmount(),
                contribution.getDescription(), contribution.getCreatedAt());
    }
}
