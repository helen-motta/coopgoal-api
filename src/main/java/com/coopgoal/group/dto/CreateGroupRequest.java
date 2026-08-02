package com.coopgoal.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
        @Schema(example = "Viagem para o Chile") @NotBlank @Size(max = 120) String name,
        @Schema(example = "Grupo para organizar as despesas da viagem") @Size(max = 500) String description
) {
}
