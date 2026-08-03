package com.coopgoal.proposal.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.goal.service.GoalService;
import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.repository.MembershipRepository;
import com.coopgoal.group.service.GroupAccessService;
import com.coopgoal.proposal.domain.Proposal;
import com.coopgoal.proposal.domain.ProposalStatus;
import com.coopgoal.proposal.domain.ProposalType;
import com.coopgoal.proposal.domain.Vote;
import com.coopgoal.proposal.domain.VoteChoice;
import com.coopgoal.proposal.exception.ExpiredProposalException;
import com.coopgoal.proposal.repository.ProposalRepository;
import com.coopgoal.proposal.repository.VoteRepository;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.user.domain.User;
import com.coopgoal.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {
    @Mock ProposalRepository proposals;
    @Mock VoteRepository votes;
    @Mock FinancialGoalRepository goals;
    @Mock MembershipRepository memberships;
    @Mock UserRepository users;
    @Mock ContributionRepository contributions;
    @Mock GroupAccessService access;
    @Mock GoalService goalService;
    @Mock AuditService audit;

    private ProposalService service;
    private Clock clock;
    private User user;
    private FinancialGoal goal;
    private UUID userId;
    private UUID proposalId;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-08-03T12:00:00Z"), ZoneOffset.UTC);
        service = new ProposalService(proposals, votes, goals, memberships, users, contributions,
                access, goalService, audit, clock);
        userId = UUID.randomUUID();
        proposalId = UUID.randomUUID();
        user = User.create("Ana", "ana@example.com", "encoded");
        ReflectionTestUtils.setField(user, "id", userId);
        CoopGroup group = CoopGroup.create("Viagem", null, user);
        ReflectionTestUtils.setField(group, "id", UUID.randomUUID());
        goal = FinancialGoal.create(group, "Passagens", null, new BigDecimal("1000.00"),
                LocalDate.of(2027, 1, 1), user);
        ReflectionTestUtils.setField(goal, "id", UUID.randomUUID());
    }

    @Test
    void rejectsDuplicateVote() {
        Proposal proposal = proposal(Instant.now(clock).plusSeconds(3600));
        when(proposals.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votes.existsByProposalIdAndUserId(proposalId, userId)).thenReturn(true);

        assertThatThrownBy(() -> service.vote(proposalId, userId, VoteChoice.APPROVE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("já votou");
    }

    @Test
    void rejectsVoteAfterExpirationAndMarksProposalExpired() {
        Proposal proposal = proposal(Instant.now(clock).minusSeconds(1));
        when(proposals.findById(proposalId)).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> service.vote(proposalId, userId, VoteChoice.APPROVE))
                .isInstanceOf(ExpiredProposalException.class);
        assertThat(proposal.getStatus()).isEqualTo(ProposalStatus.EXPIRED);
    }

    @Test
    void approvesAndAppliesProposalWithSimpleMajority() {
        Proposal proposal = proposal(Instant.now(clock).plusSeconds(3600));
        when(proposals.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votes.existsByProposalIdAndUserId(proposalId, userId)).thenReturn(false);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(votes.saveAndFlush(any(Vote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberships.countByGroupId(goal.getGroup().getId())).thenReturn(3L);
        when(votes.countByProposalIdAndChoice(proposalId, VoteChoice.APPROVE)).thenReturn(2L);
        when(votes.countByProposalIdAndChoice(proposalId, VoteChoice.REJECT)).thenReturn(0L);
        when(goals.findByIdForUpdate(goal.getId())).thenReturn(Optional.of(goal));

        service.vote(proposalId, userId, VoteChoice.APPROVE);

        assertThat(proposal.getStatus()).isEqualTo(ProposalStatus.APPROVED);
        assertThat(goal.getStatus()).isEqualTo(GoalStatus.CANCELLED);
    }

    private Proposal proposal(Instant expiresAt) {
        Proposal proposal = Proposal.create(goal, user, ProposalType.CANCEL_GOAL, null,
                "Não realizaremos mais a viagem", expiresAt);
        ReflectionTestUtils.setField(proposal, "id", proposalId);
        return proposal;
    }
}
