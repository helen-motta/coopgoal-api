package com.coopgoal.group.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.dto.AddMemberRequest;
import com.coopgoal.group.dto.CreateGroupRequest;
import com.coopgoal.group.dto.UpdateGroupRequest;
import com.coopgoal.group.repository.GroupRepository;
import com.coopgoal.group.repository.MembershipRepository;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import com.coopgoal.user.domain.User;
import com.coopgoal.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final GroupAccessService accessService;
    private final AuditService auditService;

    public GroupService(GroupRepository groupRepository, MembershipRepository membershipRepository,
                        UserRepository userRepository, GroupAccessService accessService, AuditService auditService) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @Transactional
    public CoopGroup create(UUID userId, CreateGroupRequest request) {
        User owner = findUser(userId);
        CoopGroup group = groupRepository.save(CoopGroup.create(request.name(), request.description(), owner));
        membershipRepository.save(Membership.create(group, owner, MembershipRole.OWNER));
        auditService.record(userId, "GROUP", group.getId(), "GROUP_CREATED", group.getName());
        return group;
    }

    @Transactional(readOnly = true)
    public Page<CoopGroup> list(UUID userId, Pageable pageable) {
        return groupRepository.findAllByMemberId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public CoopGroup get(UUID groupId, UUID userId) {
        accessService.requireMember(groupId, userId);
        return findGroup(groupId);
    }

    @Transactional
    public CoopGroup update(UUID groupId, UUID userId, UpdateGroupRequest request) {
        accessService.requireRole(groupId, userId, MembershipRole.OWNER, MembershipRole.ADMIN);
        CoopGroup group = findGroup(groupId);
        group.update(request.name(), request.description(), request.status());
        auditService.record(userId, "GROUP", groupId, "GROUP_UPDATED", "Dados do grupo atualizados");
        return group;
    }

    @Transactional
    public Membership addMember(UUID groupId, UUID actorId, AddMemberRequest request) {
        accessService.requireRole(groupId, actorId, MembershipRole.OWNER, MembershipRole.ADMIN);
        if (request.role() == MembershipRole.OWNER) {
            throw new BusinessRuleException("MEMBERSHIP_INVALID_ROLE", "Não é possível adicionar outro proprietário");
        }
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuário não encontrado"));
        if (membershipRepository.existsByGroupIdAndUserId(groupId, user.getId())) {
            throw new BusinessRuleException("MEMBERSHIP_DUPLICATE", "O usuário já participa deste grupo");
        }
        Membership membership = membershipRepository.save(Membership.create(findGroup(groupId), user, request.role()));
        auditService.record(actorId, "GROUP", groupId, "MEMBER_ADDED", "Usuário " + user.getId());
        return membership;
    }

    @Transactional(readOnly = true)
    public List<Membership> listMembers(UUID groupId, UUID userId) {
        accessService.requireMember(groupId, userId);
        return membershipRepository.findAllByGroupIdOrderByJoinedAtAsc(groupId);
    }

    @Transactional
    public void removeMember(UUID groupId, UUID memberUserId, UUID actorId) {
        accessService.requireRole(groupId, actorId, MembershipRole.OWNER, MembershipRole.ADMIN);
        Membership target = accessService.requireMember(groupId, memberUserId);
        if (target.getRole() == MembershipRole.OWNER) {
            throw new BusinessRuleException("OWNER_CANNOT_BE_REMOVED", "O proprietário não pode ser removido do grupo");
        }
        membershipRepository.delete(target);
        auditService.record(actorId, "GROUP", groupId, "MEMBER_REMOVED", "Usuário " + memberUserId);
    }

    private CoopGroup findGroup(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GROUP_NOT_FOUND", "Grupo não encontrado"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuário não encontrado"));
    }
}
