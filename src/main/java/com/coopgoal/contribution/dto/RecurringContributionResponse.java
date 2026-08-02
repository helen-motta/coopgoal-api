package com.coopgoal.contribution.dto;

import com.coopgoal.contribution.domain.RecurringContribution;
import com.coopgoal.contribution.domain.RecurringFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringContributionResponse(UUID id, UUID goalId, UUID memberId, BigDecimal amount,
                                            RecurringFrequency frequency, LocalDate nextExecutionDate,
                                            boolean active, Instant createdAt) {
    public static RecurringContributionResponse from(RecurringContribution recurring) {
        return new RecurringContributionResponse(recurring.getId(), recurring.getGoal().getId(),
                recurring.getMember().getId(), recurring.getAmount(), recurring.getFrequency(),
                recurring.getNextExecutionDate(), recurring.isActive(), recurring.getCreatedAt());
    }
}
