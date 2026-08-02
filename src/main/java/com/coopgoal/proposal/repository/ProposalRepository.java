package com.coopgoal.proposal.repository;

import com.coopgoal.proposal.domain.Proposal;
import com.coopgoal.proposal.domain.ProposalStatus;
import com.coopgoal.proposal.domain.ProposalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProposalRepository extends JpaRepository<Proposal, UUID> {
    @Override
    @EntityGraph(attributePaths = {"goal", "createdBy"})
    Optional<Proposal> findById(UUID id);
    boolean existsByGoalIdAndTypeAndStatus(UUID goalId, ProposalType type, ProposalStatus status);
    @EntityGraph(attributePaths = {"goal", "createdBy"})
    Page<Proposal> findAllByGoalIdAndStatus(UUID goalId, ProposalStatus status, Pageable pageable);
    @EntityGraph(attributePaths = {"goal", "createdBy"})
    Page<Proposal> findAllByGoalId(UUID goalId, Pageable pageable);
    List<Proposal> findAllByStatusAndExpiresAtBefore(ProposalStatus status, Instant instant);

    @EntityGraph(attributePaths = {"goal", "createdBy"})
    @Query("select p from Proposal p join Membership m on m.group = p.goal.group " +
            "where m.user.id = :userId and p.status = 'OPEN' " +
            "and not exists (select v.id from Vote v where v.proposal = p and v.user.id = :userId) " +
            "order by p.expiresAt")
    List<Proposal> findAwaitingVote(@Param("userId") UUID userId);
}
