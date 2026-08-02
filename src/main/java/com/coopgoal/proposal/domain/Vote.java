package com.coopgoal.proposal.domain;

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
@Table(name = "votes", uniqueConstraints =
        @UniqueConstraint(name = "uk_vote_proposal_user", columnNames = {"proposal_id", "user_id"}))
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoteChoice choice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Vote() {
    }

    public static Vote create(Proposal proposal, User user, VoteChoice choice) {
        Vote vote = new Vote();
        vote.proposal = proposal;
        vote.user = user;
        vote.choice = choice;
        return vote;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public Proposal getProposal() { return proposal; }
    public User getUser() { return user; }
    public VoteChoice getChoice() { return choice; }
    public Instant getCreatedAt() { return createdAt; }
}
