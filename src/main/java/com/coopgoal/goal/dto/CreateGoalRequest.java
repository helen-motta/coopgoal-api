package com.coopgoal.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGoalRequest(
        @Schema(example = "Passagens aéreas") @NotBlank @Size(max = 120) String name,
        @Schema(example = "Meta para compra das passagens") @Size(max = 500) String description,
        @Schema(example = "12000.00") @NotNull @DecimalMin(value = "0.01") BigDecimal targetAmount,
        @Schema(example = "2027-01-20") @NotNull @Future LocalDate deadline
) {
}
