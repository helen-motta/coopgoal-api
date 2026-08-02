package com.coopgoal.user.service;

import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.contribution.repository.RecurringContributionRepository;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.group.repository.GroupRepository;
import com.coopgoal.proposal.repository.ProposalRepository;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import com.coopgoal.user.domain.User;
import com.coopgoal.user.dto.DashboardResponse;
import com.coopgoal.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final FinancialGoalRepository goalRepository;
    private final ContributionRepository contributionRepository;
    private final RecurringContributionRepository recurringRepository;
    private final ProposalRepository proposalRepository;
    private final Clock clock;

    public UserService(UserRepository userRepository, GroupRepository groupRepository,
                       FinancialGoalRepository goalRepository, ContributionRepository contributionRepository,
                       RecurringContributionRepository recurringRepository,
                       ProposalRepository proposalRepository, Clock clock) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
        this.recurringRepository = recurringRepository;
        this.proposalRepository = proposalRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public User me(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(UUID userId) {
        LocalDate today = LocalDate.now(clock);
        BigDecimal total = contributionRepository.sumByUserId(userId);
        return new DashboardResponse(
                groupRepository.countByMemberId(userId),
                goalRepository.countByUserAndStatus(userId, GoalStatus.ACTIVE),
                goalRepository.countByUserAndStatus(userId, GoalStatus.COMPLETED),
                total,
                recurringRepository.findTop5ByMemberUserIdAndActiveTrueOrderByNextExecutionDateAsc(userId).stream()
                        .map(r -> new DashboardResponse.RecurringSummary(r.getId(), r.getGoal().getId(),
                                r.getGoal().getName(), r.getAmount(), r.getFrequency(), r.getNextExecutionDate()))
                        .toList(),
                proposalRepository.findAwaitingVote(userId).stream()
                        .map(p -> new DashboardResponse.ProposalSummary(p.getId(), p.getGoal().getId(),
                                p.getGoal().getName(), p.getType(), p.getExpiresAt()))
                        .toList(),
                goalRepository.findUpcomingForUser(userId, today, today.plusDays(30)).stream()
                        .map(g -> new DashboardResponse.GoalSummary(g.getId(), g.getName(), g.getTargetAmount(),
                                g.getDeadline(), g.getStatus()))
                        .toList());
    }
}
