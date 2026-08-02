package com.coopgoal.group.dto;

import com.coopgoal.group.domain.MembershipRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @Schema(example = "bruno@example.com") @NotBlank @Email String email,
        @Schema(example = "MEMBER") @NotNull MembershipRole role
) {
}
