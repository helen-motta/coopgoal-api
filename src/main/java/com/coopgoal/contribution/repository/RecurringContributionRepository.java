package com.coopgoal.contribution.repository;

import com.coopgoal.contribution.domain.RecurringContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurringContributionRepository extends JpaRepository<RecurringContribution, UUID> {
    List<RecurringContribution> findAllByGoalIdOrderByCreatedAtDesc(UUID goalId);
    List<RecurringContribution> findAllByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
    @EntityGraph(attributePaths = {"goal", "member", "member.user"})
    List<RecurringContribution> findTop5ByMemberUserIdAndActiveTrueOrderByNextExecutionDateAsc(UUID userId);
}
