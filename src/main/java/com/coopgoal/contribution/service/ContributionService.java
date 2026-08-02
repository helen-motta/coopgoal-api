package com.coopgoal.contribution.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.domain.Contribution;
import com.coopgoal.contribution.dto.ContributionResponse;
import com.coopgoal.contribution.exception.DuplicateContributionException;
import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.dto.GoalStatementResponse;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.service.GroupAccessService;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class ContributionService {
    private final ContributionRepository contributionRepository;
    private final FinancialGoalRepository goalRepository;
    private final GroupAccessService accessService;
    private final AuditService auditService;

    public ContributionService(ContributionRepository contributionRepository,
                               FinancialGoalRepository goalRepository,
                               GroupAccessService accessService, AuditService auditService) {
        this.contributionRepository = contributionRepository;
        this.goalRepository = goalRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @Transactional
    public Contribution register(UUID goalId, UUID userId, BigDecimal amount, String description,
                                 String idempotencyKey) {
        validateAmount(amount);
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 120) {
            throw new BusinessRuleException("IDEMPOTENCY_KEY_INVALID",
                    "O header Idempotency-Key é obrigatório e deve ter até 120 caracteres");
        }
        if (contributionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateContributionException();
        }
        FinancialGoal goal = goalRepository.findByIdForUpdate(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Meta não encontrada"));
        Membership membership = accessService.requireMember(goal.getGroup().getId(), userId);
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new BusinessRuleException("GOAL_NOT_ACTIVE", "Uma meta concluída ou cancelada não aceita contribuições");
        }

        Contribution contribution;
        try {
            contribution = contributionRepository.saveAndFlush(
                    Contribution.create(goal, membership, amount, description, idempotencyKey));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateContributionException();
        }
        auditService.record(userId, "CONTRIBUTION", contribution.getId(), "CONTRIBUTION_RECORDED",
                "Meta " + goalId + ", valor " + amount);

        BigDecimal total = contributionRepository.sumByGoalId(goalId);
        if (total.compareTo(goal.getTargetAmount()) >= 0) {
            goal.complete();
            auditService.record(userId, "GOAL", goalId, "GOAL_COMPLETED", "Conclusão automática");
        }
        return contribution;
    }

    @Transactional(readOnly = true)
    public Page<Contribution> listByGoal(UUID goalId, UUID userId, Instant from, Instant to, Pageable pageable) {
        FinancialGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Meta não encontrada"));
        accessService.requireMember(goal.getGroup().getId(), userId);
        return contributionRepository.findByGoalWithPeriod(goalId, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Contribution> listByUser(UUID userId, Pageable pageable) {
        return contributionRepository.findAllByMemberUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public GoalStatementResponse statement(UUID goalId, UUID userId, Instant from, Instant to, Pageable pageable) {
        FinancialGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Meta não encontrada"));
        accessService.requireMember(goal.getGroup().getId(), userId);
        Page<ContributionResponse> entries = contributionRepository.findByGoalWithPeriod(goalId, from, to, pageable)
                .map(ContributionResponse::from);
        return new GoalStatementResponse(goalId, goal.getTargetAmount(),
                contributionRepository.sumByGoalId(goalId), entries);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("CONTRIBUTION_INVALID_AMOUNT",
                    "O valor da contribuição deve ser maior que zero");
        }
    }
}
