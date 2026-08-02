package com.coopgoal.contribution.dto;

import com.coopgoal.contribution.domain.RecurringFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateRecurringContributionRequest(
        @DecimalMin(value = "0.01") BigDecimal amount,
        RecurringFrequency frequency,
        @FutureOrPresent LocalDate nextExecutionDate,
        Boolean active
) {
}
