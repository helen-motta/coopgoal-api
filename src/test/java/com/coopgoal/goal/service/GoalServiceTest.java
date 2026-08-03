package com.coopgoal.goal.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.dto.CreateGoalRequest;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.repository.GroupRepository;
import com.coopgoal.group.service.GroupAccessService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {
    @Mock FinancialGoalRepository goals;
    @Mock GroupRepository groups;
    @Mock UserRepository users;
    @Mock ContributionRepository contributions;
    @Mock GroupAccessService access;
    @Mock AuditService audit;

    private GoalService service;
    private UUID groupId;
    private UUID userId;
    private CoopGroup group;
    private User user;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-03T12:00:00Z"), ZoneOffset.UTC);
        service = new GoalService(goals, groups, users, contributions, access, audit, clock);
        groupId = UUID.randomUUID();
        userId = UUID.randomUUID();
        user = User.create("Ana", "ana@example.com", "encoded");
        ReflectionTestUtils.setField(user, "id", userId);
        group = CoopGroup.create("Viagem", null, user);
        ReflectionTestUtils.setField(group, "id", groupId);
    }

    @Test
    void createsGoalWhenInputAndRoleAreValid() {
        when(groups.findById(groupId)).thenReturn(Optional.of(group));
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(goals.save(any(FinancialGoal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FinancialGoal result = service.create(groupId, userId,
                new CreateGoalRequest("Passagens", null, new BigDecimal("5000.00"), LocalDate.of(2027, 1, 1)));

        assertThat(result.getTargetAmount()).isEqualByComparingTo("5000.00");
        verify(access).requireRole(groupId, userId, MembershipRole.OWNER, MembershipRole.ADMIN);
        verify(goals).save(any(FinancialGoal.class));
    }

    @Test
    void rejectsGoalWithNonFutureDeadline() {
        assertThatThrownBy(() -> service.create(groupId, userId,
                new CreateGoalRequest("Passagens", null, BigDecimal.TEN, LocalDate.of(2026, 8, 3))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("A data limite deve ser futura");
    }
}
