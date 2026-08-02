package com.coopgoal.group.dto;

import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;

import java.time.Instant;
import java.util.UUID;

public record MembershipResponse(UUID id, UUID userId, String name, String email,
                                 MembershipRole role, Instant joinedAt) {
    public static MembershipResponse from(Membership membership) {
        return new MembershipResponse(membership.getId(), membership.getUser().getId(),
                membership.getUser().getName(), membership.getUser().getEmail(),
                membership.getRole(), membership.getJoinedAt());
    }
}
