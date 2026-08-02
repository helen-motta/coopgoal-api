package com.coopgoal.group.repository;

import com.coopgoal.group.domain.CoopGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<CoopGroup, UUID> {

    @Override
    @EntityGraph(attributePaths = "owner")
    Optional<CoopGroup> findById(UUID id);

    @EntityGraph(attributePaths = "owner")
    @Query("select m.group from Membership m where m.user.id = :userId")
    Page<CoopGroup> findAllByMemberId(@Param("userId") UUID userId, Pageable pageable);

    @Query("select count(m) from Membership m where m.user.id = :userId")
    long countByMemberId(@Param("userId") UUID userId);
}
