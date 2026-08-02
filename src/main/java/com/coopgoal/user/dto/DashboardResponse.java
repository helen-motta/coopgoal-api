package com.coopgoal.user.dto;

import com.coopgoal.contribution.domain.RecurringFrequency;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.proposal.domain.ProposalType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        long groupCount,
        long activeGoalCount,
        long completedGoalCount,
        BigDecimal totalContributed,
        List<RecurringSummary> upcomingRecurringContributions,
        List<ProposalSummary> proposalsAwaitingVote,
        List<GoalSummary> goalsNearDeadline
) {
    public record RecurringSummary(UUID id, UUID goalId, String goalName, BigDecimal amount,
                                   RecurringFrequency frequency, LocalDate nextExecutionDate) { }
    public record ProposalSummary(UUID id, UUID goalId, String goalName, ProposalType type, Instant expiresAt) { }
    public record GoalSummary(UUID id, String name, BigDecimal targetAmount, LocalDate deadline, GoalStatus status) { }
}
