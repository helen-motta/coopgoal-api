package com.coopgoal.goal.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.dto.CreateGoalRequest;
import com.coopgoal.goal.dto.GoalProgressResponse;
import com.coopgoal.goal.dto.UpdateGoalRequest;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.GroupStatus;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.repository.GroupRepository;
import com.coopgoal.group.service.GroupAccessService;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import com.coopgoal.user.domain.User;
import com.coopgoal.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class GoalService {
    private final FinancialGoalRepository goalRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;
    private final GroupAccessService accessService;
    private final AuditService auditService;
    private final Clock clock;

    public GoalService(FinancialGoalRepository goalRepository, GroupRepository groupRepository,
                       UserRepository userRepository, ContributionRepository contributionRepository,
                       GroupAccessService accessService, AuditService auditService, Clock clock) {
        this.goalRepository = goalRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.contributionRepository = contributionRepository;
        this.accessService = accessService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public FinancialGoal create(UUID groupId, UUID userId, CreateGoalRequest request) {
        accessService.requireRole(groupId, userId, MembershipRole.OWNER, MembershipRole.ADMIN);
        validateTarget(request.targetAmount());
        validateDeadline(request.deadline());
        CoopGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("GROUP_NOT_FOUND", "Grupo não encontrado"));
        if (group.getStatus() != GroupStatus.ACTIVE) {
            throw new BusinessRuleException("GROUP_ARCHIVED", "Não é possível criar metas em um grupo arquivado");
        }
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuário não encontrado"));
        FinancialGoal goal = goalRepository.save(FinancialGoal.create(group, request.name(), request.description(),
                request.targetAmount(), request.deadline(), creator));
        auditService.record(userId, "GOAL", goal.getId(), "GOAL_CREATED", goal.getName());
        return goal;
    }

    @Transactional(readOnly = true)
    public Page<FinancialGoal> list(UUID groupId, UUID userId, GoalStatus status, LocalDate deadlineFrom,
                                    LocalDate deadlineTo, String name, Pageable pageable) {
        accessService.requireMember(groupId, userId);
        Specification<FinancialGoal> spec = (root, query, cb) -> cb.equal(root.get("group").get("id"), groupId);
        if (status != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        if (deadlineFrom != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("deadline"), deadlineFrom));
        if (deadlineTo != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("deadline"), deadlineTo));
        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }
        return goalRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public FinancialGoal get(UUID goalId, UUID userId) {
        FinancialGoal goal = find(goalId);
        accessService.requireMember(goal.getGroup().getId(), userId);
        return goal;
    }

    @Transactional
    public FinancialGoal update(UUID goalId, UUID userId, UpdateGoalRequest request) {
        FinancialGoal goal = find(goalId);
        accessService.requireRole(goal.getGroup().getId(), userId, MembershipRole.OWNER, MembershipRole.ADMIN);
        goal.updateDetails(request.name(), request.description());
        auditService.record(userId, "GOAL", goalId, "GOAL_UPDATED", "Nome ou descrição atualizados");
        return goal;
    }

    @Transactional(readOnly = true)
    public GoalProgressResponse progress(UUID goalId, UUID userId) {
        FinancialGoal goal = get(goalId, userId);
        BigDecimal total = contributionRepository.sumByGoalId(goalId);
        BigDecimal remaining = goal.getTargetAmount().subtract(total).max(BigDecimal.ZERO);
        BigDecimal percentage = total.multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        return new GoalProgressResponse(goalId, goal.getTargetAmount(), total, remaining, percentage, goal.getStatus());
    }

    public void validateTarget(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("GOAL_INVALID_TARGET", "O valor-alvo deve ser maior que zero");
        }
    }

    public void validateDeadline(LocalDate deadline) {
        if (deadline == null || !deadline.isAfter(LocalDate.now(clock))) {
            throw new BusinessRuleException("GOAL_INVALID_DEADLINE", "A data limite deve ser futura");
        }
    }

    private FinancialGoal find(UUID id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Meta não encontrada"));
    }
}
