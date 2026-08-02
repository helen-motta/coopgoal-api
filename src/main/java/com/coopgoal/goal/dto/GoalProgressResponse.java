package com.coopgoal.goal.dto;

import com.coopgoal.goal.domain.GoalStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record GoalProgressResponse(UUID goalId, BigDecimal targetAmount, BigDecimal contributedAmount,
                                   BigDecimal remainingAmount, BigDecimal percentage, GoalStatus status) {
}
