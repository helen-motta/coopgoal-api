package com.coopgoal.group.domain;

import com.coopgoal.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "memberships", uniqueConstraints =
        @UniqueConstraint(name = "uk_membership_group_user", columnNames = {"group_id", "user_id"}))
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private CoopGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected Membership() {
    }

    public static Membership create(CoopGroup group, User user, MembershipRole role) {
        Membership membership = new Membership();
        membership.group = group;
        membership.user = user;
        membership.role = role;
        return membership;
    }

    public void changeRole(MembershipRole role) { this.role = role; }

    @PrePersist
    void onCreate() { joinedAt = Instant.now(); }

    public UUID getId() { return id; }
    public CoopGroup getGroup() { return group; }
    public User getUser() { return user; }
    public MembershipRole getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }
}
