package com.coopgoal.contribution.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.domain.RecurringContribution;
import com.coopgoal.contribution.dto.CreateRecurringContributionRequest;
import com.coopgoal.contribution.dto.UpdateRecurringContributionRequest;
import com.coopgoal.contribution.repository.RecurringContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.service.GroupAccessService;
import com.coopgoal.shared.exception.AccessDeniedException;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class RecurringContributionService {
    private final RecurringContributionRepository repository;
    private final FinancialGoalRepository goalRepository;
    private final GroupAccessService accessService;
    private final AuditService auditService;

    public RecurringContributionService(RecurringContributionRepository repository,
                                        FinancialGoalRepository goalRepository,
                                        GroupAccessService accessService, AuditService auditService) {
        this.repository = repository;
        this.goalRepository = goalRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @Transactional
    public RecurringContribution create(UUID goalId, UUID userId, CreateRecurringContributionRequest request) {
        validateAmount(request.amount());
        FinancialGoal goal = findGoal(goalId);
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new BusinessRuleException("GOAL_NOT_ACTIVE", "A meta precisa estar ativa");
        }
        Membership member = accessService.requireMember(goal.getGroup().getId(), userId);
        RecurringContribution recurring = repository.save(RecurringContribution.create(goal, member,
                request.amount(), request.frequency(), request.nextExecutionDate()));
        auditService.record(userId, "RECURRING_CONTRIBUTION", recurring.getId(), "RECURRING_CREATED",
                "Meta " + goalId);
        return recurring;
    }

    @Transactional(readOnly = true)
    public List<RecurringContribution> list(UUID goalId, UUID userId) {
        FinancialGoal goal = findGoal(goalId);
        accessService.requireMember(goal.getGroup().getId(), userId);
        return repository.findAllByGoalIdOrderByCreatedAtDesc(goalId);
    }

    @Transactional
    public RecurringContribution update(UUID id, UUID userId, UpdateRecurringContributionRequest request) {
        RecurringContribution recurring = find(id);
        requireOwnerOrManager(recurring, userId);
        if (request.amount() != null) validateAmount(request.amount());
        recurring.update(request.amount(), request.frequency(), request.nextExecutionDate(), request.active());
        auditService.record(userId, "RECURRING_CONTRIBUTION", id, "RECURRING_UPDATED", "Configuração atualizada");
        return recurring;
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        RecurringContribution recurring = find(id);
        requireOwnerOrManager(recurring, userId);
        repository.delete(recurring);
        auditService.record(userId, "RECURRING_CONTRIBUTION", id, "RECURRING_DELETED", "Recorrência removida");
    }

    private void requireOwnerOrManager(RecurringContribution recurring, UUID userId) {
        accessService.requireMember(recurring.getGoal().getGroup().getId(), userId);
        if (!recurring.getMember().getUser().getId().equals(userId)) {
            try {
                accessService.requireRole(recurring.getGoal().getGroup().getId(), userId,
                        MembershipRole.OWNER, MembershipRole.ADMIN);
            } catch (AccessDeniedException ex) {
                throw new AccessDeniedException("Somente o titular ou um gestor do grupo pode alterar a recorrência");
            }
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("RECURRING_INVALID_AMOUNT", "O valor deve ser maior que zero");
        }
    }

    private FinancialGoal findGoal(UUID id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Meta não encontrada"));
    }

    private RecurringContribution find(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("RECURRING_NOT_FOUND", "Contribuição recorrente não encontrada"));
    }
}
