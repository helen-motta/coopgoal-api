package com.coopgoal.contribution.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.domain.RecurringContribution;
import com.coopgoal.contribution.repository.RecurringContributionRepository;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class RecurringContributionProcessor {
    private final RecurringContributionRepository repository;
    private final ContributionService contributionService;
    private final AuditService auditService;

    public RecurringContributionProcessor(RecurringContributionRepository repository,
                                          ContributionService contributionService, AuditService auditService) {
        this.repository = repository;
        this.contributionService = contributionService;
        this.auditService = auditService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(UUID recurringId) {
        RecurringContribution recurring = repository.findById(recurringId).orElseThrow(() ->
                new ResourceNotFoundException("RECURRING_NOT_FOUND", "Contribuição recorrente não encontrada"));
        if (!recurring.isActive()) return;
        if (recurring.getGoal().getStatus() != GoalStatus.ACTIVE) {
            recurring.deactivate();
            auditService.record(recurring.getMember().getUser().getId(), "RECURRING_CONTRIBUTION", recurringId,
                    "RECURRING_DEACTIVATED", "Meta não está ativa");
            return;
        }
        LocalDate executionDate = recurring.getNextExecutionDate();
        String key = "recurring:" + recurringId + ":" + executionDate;
        contributionService.register(recurring.getGoal().getId(), recurring.getMember().getUser().getId(),
                recurring.getAmount(), "Contribuição recorrente", key);
        recurring.advance();
    }
}
