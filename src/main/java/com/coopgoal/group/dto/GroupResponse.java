package com.coopgoal.group.dto;

import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.GroupStatus;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(UUID id, String name, String description, UUID ownerId, String ownerName,
                            GroupStatus status, Instant createdAt, Instant updatedAt) {
    public static GroupResponse from(CoopGroup group) {
        return new GroupResponse(group.getId(), group.getName(), group.getDescription(),
                group.getOwner().getId(), group.getOwner().getName(), group.getStatus(),
                group.getCreatedAt(), group.getUpdatedAt());
    }
}
