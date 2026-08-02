package com.coopgoal.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(example = "Ana Silva") @NotBlank @Size(max = 120) String name,
        @Schema(example = "ana@example.com") @NotBlank @Email @Size(max = 255) String email,
        @Schema(example = "SenhaForte@123") @NotBlank @Size(min = 8, max = 72) String password
) {
}
