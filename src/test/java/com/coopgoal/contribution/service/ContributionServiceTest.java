package com.coopgoal.contribution.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.domain.Contribution;
import com.coopgoal.contribution.exception.DuplicateContributionException;
import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.service.GroupAccessService;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContributionServiceTest {
    @Mock ContributionRepository contributions;
    @Mock FinancialGoalRepository goals;
    @Mock GroupAccessService access;
    @Mock AuditService audit;

    private ContributionService service;
    private FinancialGoal goal;
    private Membership membership;
    private UUID goalId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new ContributionService(contributions, goals, access, audit);
        userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        goalId = UUID.randomUUID();
        User user = User.create("Ana", "ana@example.com", "encoded");
        ReflectionTestUtils.setField(user, "id", userId);
        CoopGroup group = CoopGroup.create("Viagem", null, user);
        ReflectionTestUtils.setField(group, "id", groupId);
        membership = Membership.create(group, user, MembershipRole.OWNER);
        ReflectionTestUtils.setField(membership, "id", UUID.randomUUID());
        goal = FinancialGoal.create(group, "Passagens", null, new BigDecimal("1000.00"),
                LocalDate.now().plusMonths(2), user);
        ReflectionTestUtils.setField(goal, "id", goalId);
    }

    @Test
    void recordsValidContribution() {
        arrangeActiveGoal(new BigDecimal("250.00"));

        Contribution result = service.register(goalId, userId, new BigDecimal("250.00"),
                "Agosto", "key-1");

        assertThat(result.getAmount()).isEqualByComparingTo("250.00");
        assertThat(goal.getStatus()).isEqualTo(GoalStatus.ACTIVE);
        verify(contributions).saveAndFlush(any(Contribution.class));
    }

    @Test
    void rejectsDuplicateIdempotencyKey() {
        when(contributions.existsByIdempotencyKey("same-key")).thenReturn(true);

        assertThatThrownBy(() -> service.register(goalId, userId, BigDecimal.TEN, null, "same-key"))
                .isInstanceOf(DuplicateContributionException.class);
        verify(goals, never()).findByIdForUpdate(any());
    }

    @Test
    void completesGoalWhenTotalReachesTarget() {
        arrangeActiveGoal(new BigDecimal("1000.00"));

        service.register(goalId, userId, new BigDecimal("400.00"), null, "key-complete");

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void rejectsContributionToCancelledGoal() {
        goal.cancel();
        when(contributions.existsByIdempotencyKey("key-cancelled")).thenReturn(false);
        when(goals.findByIdForUpdate(goalId)).thenReturn(Optional.of(goal));
        when(access.requireMember(goal.getGroup().getId(), userId)).thenReturn(membership);

        assertThatThrownBy(() -> service.register(goalId, userId, BigDecimal.TEN, null, "key-cancelled"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não aceita contribuições");
        verify(contributions, never()).saveAndFlush(any());
    }

    private void arrangeActiveGoal(BigDecimal total) {
        when(contributions.existsByIdempotencyKey(any())).thenReturn(false);
        when(goals.findByIdForUpdate(goalId)).thenReturn(Optional.of(goal));
        when(access.requireMember(goal.getGroup().getId(), userId)).thenReturn(membership);
        when(contributions.saveAndFlush(any(Contribution.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contributions.sumByGoalId(goalId)).thenReturn(total);
    }
}
