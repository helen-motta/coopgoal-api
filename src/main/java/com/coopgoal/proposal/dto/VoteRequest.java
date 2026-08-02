package com.coopgoal.proposal.dto;

import com.coopgoal.proposal.domain.VoteChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record VoteRequest(@Schema(example = "APPROVE") @NotNull VoteChoice choice) {
}
