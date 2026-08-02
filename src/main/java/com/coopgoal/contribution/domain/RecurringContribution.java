package com.coopgoal.contribution.domain;

import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.group.domain.Membership;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "recurring_contributions")
public class RecurringContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private FinancialGoal goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Membership member;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurringFrequency frequency;

    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RecurringContribution() {
    }

    public static RecurringContribution create(FinancialGoal goal, Membership member, BigDecimal amount,
                                               RecurringFrequency frequency, LocalDate nextExecutionDate) {
        RecurringContribution recurring = new RecurringContribution();
        recurring.goal = goal;
        recurring.member = member;
        recurring.amount = amount;
        recurring.frequency = frequency;
        recurring.nextExecutionDate = nextExecutionDate;
        recurring.active = true;
        return recurring;
    }

    public void update(BigDecimal amount, RecurringFrequency frequency, LocalDate nextExecutionDate, Boolean active) {
        if (amount != null) this.amount = amount;
        if (frequency != null) this.frequency = frequency;
        if (nextExecutionDate != null) this.nextExecutionDate = nextExecutionDate;
        if (active != null) this.active = active;
    }

    public void advance() {
        nextExecutionDate = frequency == RecurringFrequency.WEEKLY
                ? nextExecutionDate.plusWeeks(1)
                : nextExecutionDate.plusMonths(1);
    }

    public void deactivate() { active = false; }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public FinancialGoal getGoal() { return goal; }
    public Membership getMember() { return member; }
    public BigDecimal getAmount() { return amount; }
    public RecurringFrequency getFrequency() { return frequency; }
    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}
