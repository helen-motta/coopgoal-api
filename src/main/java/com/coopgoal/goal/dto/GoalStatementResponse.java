package com.coopgoal.goal.dto;

import com.coopgoal.contribution.dto.ContributionResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalStatementResponse(UUID goalId, BigDecimal targetAmount, BigDecimal totalContributed,
                                    Page<ContributionResponse> entries) {
}
