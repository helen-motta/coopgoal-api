package com.coopgoal.goal.repository;

import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.domain.GoalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID>,
        JpaSpecificationExecutor<FinancialGoal> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from FinancialGoal g where g.id = :id")
    Optional<FinancialGoal> findByIdForUpdate(@Param("id") UUID id);

    @Query("select count(distinct g) from FinancialGoal g join Membership m on m.group = g.group " +
            "where m.user.id = :userId and g.status = :status")
    long countByUserAndStatus(@Param("userId") UUID userId, @Param("status") GoalStatus status);

    @Query("select g from FinancialGoal g join Membership m on m.group = g.group " +
            "where m.user.id = :userId and g.status = 'ACTIVE' and g.deadline between :from and :to " +
            "order by g.deadline")
    List<FinancialGoal> findUpcomingForUser(@Param("userId") UUID userId,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);
}
