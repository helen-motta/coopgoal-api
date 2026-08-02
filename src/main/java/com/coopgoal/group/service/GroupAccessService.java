package com.coopgoal.group.service;

import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.repository.MembershipRepository;
import com.coopgoal.shared.exception.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class GroupAccessService {
    private final MembershipRepository membershipRepository;

    public GroupAccessService(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    public Membership requireMember(UUID groupId, UUID userId) {
        return membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new AccessDeniedException("Apenas membros do grupo podem acessar este recurso"));
    }

    public Membership requireRole(UUID groupId, UUID userId, MembershipRole... allowed) {
        Membership membership = requireMember(groupId, userId);
        if (!Set.of(allowed).contains(membership.getRole())) {
            throw new AccessDeniedException("Seu papel no grupo não permite esta operação");
        }
        return membership;
    }
}
