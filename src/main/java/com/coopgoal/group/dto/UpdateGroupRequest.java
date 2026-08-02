package com.coopgoal.group.dto;

import com.coopgoal.group.domain.GroupStatus;
import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(
        @Size(max = 120) String name,
        @Size(max = 500) String description,
        GroupStatus status
) {
}
