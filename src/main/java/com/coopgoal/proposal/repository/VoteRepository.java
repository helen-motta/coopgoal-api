package com.coopgoal.proposal.repository;

import com.coopgoal.proposal.domain.Vote;
import com.coopgoal.proposal.domain.VoteChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    boolean existsByProposalIdAndUserId(UUID proposalId, UUID userId);
    long countByProposalIdAndChoice(UUID proposalId, VoteChoice choice);
    @EntityGraph(attributePaths = "user")
    List<Vote> findAllByProposalIdOrderByCreatedAtAsc(UUID proposalId);
}
