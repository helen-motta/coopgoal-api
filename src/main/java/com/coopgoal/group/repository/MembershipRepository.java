package com.coopgoal.group.repository;

import com.coopgoal.group.domain.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    @EntityGraph(attributePaths = "user")
    Optional<Membership> findByGroupIdAndUserId(UUID groupId, UUID userId);
    @EntityGraph(attributePaths = "user")
    List<Membership> findAllByGroupIdOrderByJoinedAtAsc(UUID groupId);
    long countByGroupId(UUID groupId);
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
}
