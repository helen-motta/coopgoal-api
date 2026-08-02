package com.coopgoal.contribution.repository;

import com.coopgoal.contribution.domain.Contribution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ContributionRepository extends JpaRepository<Contribution, UUID> {
    boolean existsByIdempotencyKey(String idempotencyKey);
    Optional<Contribution> findByIdempotencyKey(String idempotencyKey);

    @Query("select coalesce(sum(c.amount), 0) from Contribution c where c.goal.id = :goalId")
    BigDecimal sumByGoalId(@Param("goalId") UUID goalId);

    @Query("select coalesce(sum(c.amount), 0) from Contribution c where c.member.user.id = :userId")
    BigDecimal sumByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"goal", "member", "member.user"})
    @Query("select c from Contribution c where c.goal.id = :goalId " +
            "and (cast(:from as instant) is null or c.createdAt >= :from) " +
            "and (cast(:to as instant) is null or c.createdAt <= :to)")
    Page<Contribution> findByGoalWithPeriod(@Param("goalId") UUID goalId,
                                            @Param("from") Instant from,
                                            @Param("to") Instant to,
                                            Pageable pageable);

    @EntityGraph(attributePaths = {"goal", "member", "member.user"})
    Page<Contribution> findAllByMemberUserId(UUID userId, Pageable pageable);
}
