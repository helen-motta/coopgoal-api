package com.coopgoal.contribution.service;

import com.coopgoal.contribution.domain.RecurringContribution;
import com.coopgoal.contribution.repository.RecurringContributionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
public class RecurringContributionScheduler {
    private static final Logger log = LoggerFactory.getLogger(RecurringContributionScheduler.class);

    private final RecurringContributionRepository repository;
    private final RecurringContributionProcessor processor;
    private final Clock clock;
    private final Counter successCounter;
    private final Counter failureCounter;

    public RecurringContributionScheduler(RecurringContributionRepository repository,
                                          RecurringContributionProcessor processor,
                                          Clock clock, MeterRegistry registry) {
        this.repository = repository;
        this.processor = processor;
        this.clock = clock;
        this.successCounter = registry.counter("coopgoal.recurring.processed", "result", "success");
        this.failureCounter = registry.counter("coopgoal.recurring.processed", "result", "failure");
    }

    @Scheduled(fixedDelayString = "${coopgoal.recurring.fixed-delay}")
    public void processDueContributions() {
        List<RecurringContribution> due = repository
                .findAllByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate.now(clock));
        for (RecurringContribution recurring : due) {
            try {
                processor.process(recurring.getId());
                successCounter.increment();
            } catch (RuntimeException ex) {
                failureCounter.increment();
                log.error("recurring_contribution_failed recurringId={} reason={}",
                        recurring.getId(), ex.getMessage());
            }
        }
    }
}
