package com.coopgoal.goal.dto;

import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GoalResponse(UUID id, UUID groupId, String name, String description, BigDecimal targetAmount,
                           LocalDate deadline, GoalStatus status, UUID createdBy, Instant createdAt,
                           Instant updatedAt, long version) {
    public static GoalResponse from(FinancialGoal goal) {
        return new GoalResponse(goal.getId(), goal.getGroup().getId(), goal.getName(), goal.getDescription(),
                goal.getTargetAmount(), goal.getDeadline(), goal.getStatus(), goal.getCreatedBy().getId(),
                goal.getCreatedAt(), goal.getUpdatedAt(), goal.getVersion());
    }
}
