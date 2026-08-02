package com.coopgoal.proposal.domain;

import com.coopgoal.goal.domain.FinancialGoal;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "proposals")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private FinancialGoal goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProposalType type;

    @Column(name = "proposed_value", length = 255)
    private String proposedValue;

    @Column(nullable = false, length = 1000)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Proposal() {
    }

    public static Proposal create(FinancialGoal goal, User createdBy, ProposalType type,
                                  String proposedValue, String justification, Instant expiresAt) {
        Proposal proposal = new Proposal();
        proposal.goal = goal;
        proposal.createdBy = createdBy;
        proposal.type = type;
        proposal.proposedValue = proposedValue;
        proposal.justification = justification.trim();
        proposal.status = ProposalStatus.OPEN;
        proposal.expiresAt = expiresAt;
        return proposal;
    }

    public void approve() { status = ProposalStatus.APPROVED; }
    public void reject() { status = ProposalStatus.REJECTED; }
    public void expire() { status = ProposalStatus.EXPIRED; }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public FinancialGoal getGoal() { return goal; }
    public User getCreatedBy() { return createdBy; }
    public ProposalType getType() { return type; }
    public String getProposedValue() { return proposedValue; }
    public String getJustification() { return justification; }
    public ProposalStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
