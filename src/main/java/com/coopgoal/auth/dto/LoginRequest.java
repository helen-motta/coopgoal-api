package com.coopgoal.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "ana@example.com") @NotBlank @Email String email,
        @Schema(example = "SenhaForte@123") @NotBlank String password
) {
}
