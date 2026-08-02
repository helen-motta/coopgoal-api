package com.coopgoal.contribution.dto;

import com.coopgoal.contribution.domain.RecurringFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRecurringContributionRequest(
        @Schema(example = "100.00") @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Schema(example = "MONTHLY") @NotNull RecurringFrequency frequency,
        @Schema(example = "2026-09-01") @NotNull @FutureOrPresent LocalDate nextExecutionDate
) {
}
