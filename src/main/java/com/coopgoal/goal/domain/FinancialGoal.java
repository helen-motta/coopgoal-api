package com.coopgoal.goal.domain;

import com.coopgoal.group.domain.CoopGroup;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_goals")
public class FinancialGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private CoopGroup group;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected FinancialGoal() {
    }

    public static FinancialGoal create(CoopGroup group, String name, String description,
                                       BigDecimal targetAmount, LocalDate deadline, User createdBy) {
        FinancialGoal goal = new FinancialGoal();
        goal.group = group;
        goal.name = name.trim();
        goal.description = description;
        goal.targetAmount = targetAmount;
        goal.deadline = deadline;
        goal.status = GoalStatus.ACTIVE;
        goal.createdBy = createdBy;
        return goal;
    }

    public void updateDetails(String name, String description) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (description != null) this.description = description;
    }

    public void changeTargetAmount(BigDecimal value) { this.targetAmount = value; }
    public void changeDeadline(LocalDate value) { this.deadline = value; }
    public void complete() { this.status = GoalStatus.COMPLETED; }
    public void cancel() { this.status = GoalStatus.CANCELLED; }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public CoopGroup getGroup() { return group; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public LocalDate getDeadline() { return deadline; }
    public GoalStatus getStatus() { return status; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
