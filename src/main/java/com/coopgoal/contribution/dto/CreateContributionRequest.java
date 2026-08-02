package com.coopgoal.contribution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateContributionRequest(
        @Schema(example = "250.00") @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Schema(example = "Contribuição de agosto") @Size(max = 500) String description
) {
}
