package com.coopgoal.proposal.dto;

import com.coopgoal.proposal.domain.ProposalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateProposalRequest(
        @Schema(example = "CHANGE_TARGET_AMOUNT") @NotNull ProposalType type,
        @Schema(example = "15000.00") @Size(max = 255) String proposedValue,
        @Schema(example = "Os custos estimados aumentaram") @NotBlank @Size(max = 1000) String justification,
        @Schema(example = "2026-08-10T18:00:00Z") @NotNull @Future Instant expiresAt
) {
}
