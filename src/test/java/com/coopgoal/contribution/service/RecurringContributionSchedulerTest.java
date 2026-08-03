package com.coopgoal.contribution.service;

import com.coopgoal.contribution.domain.RecurringContribution;
import com.coopgoal.contribution.domain.RecurringFrequency;
import com.coopgoal.contribution.repository.RecurringContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.user.domain.User;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecurringContributionSchedulerTest {
    @Test
    void continuesProcessingAfterOneRecurringContributionFails() {
        RecurringContributionRepository repository = mock(RecurringContributionRepository.class);
        RecurringContributionProcessor processor = mock(RecurringContributionProcessor.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-03T12:00:00Z"), ZoneOffset.UTC);
        RecurringContribution first = recurring();
        RecurringContribution second = recurring();
        when(repository.findAllByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate.of(2026, 8, 3)))
                .thenReturn(List.of(first, second));
        doThrow(new RuntimeException("falha simulada")).when(processor).process(first.getId());
        RecurringContributionScheduler scheduler = new RecurringContributionScheduler(repository, processor,
                clock, new SimpleMeterRegistry());

        scheduler.processDueContributions();

        verify(processor).process(first.getId());
        verify(processor).process(second.getId());
    }

    private RecurringContribution recurring() {
        User user = User.create("Ana", "ana" + UUID.randomUUID() + "@example.com", "encoded");
        CoopGroup group = CoopGroup.create("Grupo", null, user);
        Membership member = Membership.create(group, user, MembershipRole.MEMBER);
        FinancialGoal goal = FinancialGoal.create(group, "Meta", null, BigDecimal.TEN,
                LocalDate.of(2027, 1, 1), user);
        RecurringContribution recurring = RecurringContribution.create(goal, member, BigDecimal.ONE,
                RecurringFrequency.MONTHLY, LocalDate.of(2026, 8, 3));
        ReflectionTestUtils.setField(recurring, "id", UUID.randomUUID());
        return recurring;
    }
}
