package com.coopgoal.proposal.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.goal.service.GoalService;
import com.coopgoal.group.repository.MembershipRepository;
import com.coopgoal.group.service.GroupAccessService;
import com.coopgoal.proposal.domain.Proposal;
import com.coopgoal.proposal.domain.ProposalStatus;
import com.coopgoal.proposal.domain.ProposalType;
import com.coopgoal.proposal.domain.Vote;
import com.coopgoal.proposal.domain.VoteChoice;
import com.coopgoal.proposal.dto.CreateProposalRequest;
import com.coopgoal.proposal.exception.ExpiredProposalException;
import com.coopgoal.proposal.repository.ProposalRepository;
import com.coopgoal.proposal.repository.VoteRepository;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import com.coopgoal.user.domain.User;
import com.coopgoal.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
public class ProposalService {
    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final FinancialGoalRepository goalRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final ContributionRepository contributionRepository;
    private final GroupAccessService accessService;
    private final GoalService goalService;
    private final AuditService auditService;
    private final Clock clock;

    public ProposalService(ProposalRepository proposalRepository, VoteRepository voteRepository,
                           FinancialGoalRepository goalRepository, MembershipRepository membershipRepository,
                           UserRepository userRepository, ContributionRepository contributionRepository,
                           GroupAccessService accessService, GoalService goalService,
                           AuditService auditService, Clock clock) {
        this.proposalRepository = proposalRepository;
        this.voteRepository = voteRepository;
        this.goalRepository = goalRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.contributionRepository = contributionRepository;
        this.accessService = accessService;
        this.goalService = goalService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public Proposal create(UUID goalId, UUID userId, CreateProposalRequest request) {
        FinancialGoal goal = findGoal(goalId);
        accessService.requireMember(goal.getGroup().getId(), userId);
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new BusinessRuleException("GOAL_NOT_ACTIVE", "Apenas metas ativas podem receber propostas");
        }
        if (proposalRepository.existsByGoalIdAndTypeAndStatus(goalId, request.type(), ProposalStatus.OPEN)) {
            throw new BusinessRuleException("PROPOSAL_ALREADY_OPEN",
                    "Já existe uma proposta aberta deste tipo para a meta");
        }
        validateProposedValue(request.type(), request.proposedValue());
        User creator = findUser(userId);
        Proposal proposal = proposalRepository.save(Proposal.create(goal, creator, request.type(),
                request.proposedValue(), request.justification(), request.expiresAt()));
        auditService.record(userId, "PROPOSAL", proposal.getId(), "PROPOSAL_CREATED", request.type().name());
        return proposal;
    }

    @Transactional(readOnly = true)
    public Page<Proposal> list(UUID goalId, UUID userId, ProposalStatus status, Pageable pageable) {
        FinancialGoal goal = findGoal(goalId);
        accessService.requireMember(goal.getGroup().getId(), userId);
        return status == null ? proposalRepository.findAllByGoalId(goalId, pageable)
                : proposalRepository.findAllByGoalIdAndStatus(goalId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Proposal get(UUID proposalId, UUID userId) {
        Proposal proposal = find(proposalId);
        accessService.requireMember(proposal.getGoal().getGroup().getId(), userId);
        return proposal;
    }

    @Transactional(noRollbackFor = ExpiredProposalException.class)
    public Vote vote(UUID proposalId, UUID userId, VoteChoice choice) {
        Proposal proposal = find(proposalId);
        accessService.requireMember(proposal.getGoal().getGroup().getId(), userId);
        if (proposal.getStatus() != ProposalStatus.OPEN) {
            throw new BusinessRuleException("PROPOSAL_NOT_OPEN", "A proposta não está aberta");
        }
        if (!proposal.getExpiresAt().isAfter(Instant.now(clock))) {
            proposal.expire();
            auditService.record(userId, "PROPOSAL", proposalId, "PROPOSAL_EXPIRED", "Expiração detectada no voto");
            throw new ExpiredProposalException();
        }
        if (voteRepository.existsByProposalIdAndUserId(proposalId, userId)) {
            throw new BusinessRuleException("VOTE_DUPLICATE", "O usuário já votou nesta proposta");
        }
        Vote vote;
        try {
            vote = voteRepository.saveAndFlush(Vote.create(proposal, findUser(userId), choice));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("VOTE_DUPLICATE", "O usuário já votou nesta proposta");
        }
        auditService.record(userId, "PROPOSAL", proposalId, "VOTE_RECORDED", choice.name());
        evaluate(proposal, userId);
        return vote;
    }

    @Transactional(readOnly = true)
    public List<Vote> votes(UUID proposalId) {
        return voteRepository.findAllByProposalIdOrderByCreatedAtAsc(proposalId);
    }

    private void evaluate(Proposal proposal, UUID actorId) {
        long members = membershipRepository.countByGroupId(proposal.getGoal().getGroup().getId());
        long approvals = voteRepository.countByProposalIdAndChoice(proposal.getId(), VoteChoice.APPROVE);
        long rejections = voteRepository.countByProposalIdAndChoice(proposal.getId(), VoteChoice.REJECT);
        if (approvals > members / 2) {
            proposal.approve();
            apply(proposal, actorId);
            auditService.record(actorId, "PROPOSAL", proposal.getId(), "PROPOSAL_APPROVED",
                    approvals + " de " + members + " membros");
        } else if (rejections > members / 2) {
            proposal.reject();
            auditService.record(actorId, "PROPOSAL", proposal.getId(), "PROPOSAL_REJECTED",
                    rejections + " de " + members + " membros");
        }
    }

    private void apply(Proposal proposal, UUID actorId) {
        FinancialGoal goal = goalRepository.findByIdForUpdate(proposal.getGoal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Meta não encontrada"));
        switch (proposal.getType()) {
            case CHANGE_TARGET_AMOUNT -> {
                BigDecimal amount = new BigDecimal(proposal.getProposedValue());
                goal.changeTargetAmount(amount);
                if (contributionRepository.sumByGoalId(goal.getId()).compareTo(amount) >= 0) goal.complete();
            }
            case CHANGE_DEADLINE -> goal.changeDeadline(LocalDate.parse(proposal.getProposedValue()));
            case CANCEL_GOAL -> goal.cancel();
        }
        auditService.record(actorId, "GOAL", goal.getId(), "PROPOSAL_APPLIED", proposal.getType().name());
    }

    private void validateProposedValue(ProposalType type, String value) {
        try {
            switch (type) {
                case CHANGE_TARGET_AMOUNT -> goalService.validateTarget(new BigDecimal(value));
                case CHANGE_DEADLINE -> goalService.validateDeadline(LocalDate.parse(value));
                case CANCEL_GOAL -> {
                    if (value != null && !value.isBlank()) {
                        throw new BusinessRuleException("PROPOSAL_VALUE_INVALID",
                                "O cancelamento não deve informar um valor proposto");
                    }
                }
            }
        } catch (NumberFormatException | DateTimeParseException | NullPointerException ex) {
            throw new BusinessRuleException("PROPOSAL_VALUE_INVALID", "O valor proposto é inválido para este tipo");
        }
    }

    private Proposal find(UUID id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PROPOSAL_NOT_FOUND", "Proposta não encontrada"));
    }

    private FinancialGoal findGoal(UUID id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GOAL_NOT_FOUND", "Meta não encontrada"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuário não encontrado"));
    }
}
