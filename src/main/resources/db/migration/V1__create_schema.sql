CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE UNIQUE INDEX uk_users_email_lower ON users (LOWER(email));
CREATE INDEX idx_users_created_at ON users (created_at);

CREATE TABLE coop_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    owner_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_groups_owner_id ON coop_groups (owner_id);
CREATE INDEX idx_groups_status ON coop_groups (status);
CREATE INDEX idx_groups_created_at ON coop_groups (created_at);

CREATE TABLE memberships (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES coop_groups(id),
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),
    joined_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_membership_group_user UNIQUE (group_id, user_id)
);

CREATE INDEX idx_memberships_group_id ON memberships (group_id);
CREATE INDEX idx_memberships_user_id ON memberships (user_id);

CREATE TABLE financial_goals (
    id UUID PRIMARY KEY,
    group_id UUID NOT NULL REFERENCES coop_groups(id),
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    target_amount NUMERIC(19,2) NOT NULL CHECK (target_amount > 0),
    deadline DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_goals_group_id ON financial_goals (group_id);
CREATE INDEX idx_goals_status ON financial_goals (status);
CREATE INDEX idx_goals_deadline ON financial_goals (deadline);
CREATE INDEX idx_goals_created_at ON financial_goals (created_at);
CREATE INDEX idx_goals_group_status_deadline ON financial_goals (group_id, status, deadline);

CREATE TABLE contributions (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES financial_goals(id),
    member_id UUID NOT NULL REFERENCES memberships(id),
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    description VARCHAR(500),
    idempotency_key VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_contribution_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_contributions_goal_id ON contributions (goal_id);
CREATE INDEX idx_contributions_member_id ON contributions (member_id);
CREATE INDEX idx_contributions_created_at ON contributions (created_at);
CREATE INDEX idx_contributions_goal_created ON contributions (goal_id, created_at);

CREATE TABLE recurring_contributions (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES financial_goals(id),
    member_id UUID NOT NULL REFERENCES memberships(id),
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    frequency VARCHAR(20) NOT NULL CHECK (frequency IN ('WEEKLY', 'MONTHLY')),
    next_execution_date DATE NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_recurring_goal_id ON recurring_contributions (goal_id);
CREATE INDEX idx_recurring_member_id ON recurring_contributions (member_id);
CREATE INDEX idx_recurring_due ON recurring_contributions (active, next_execution_date);

CREATE TABLE proposals (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES financial_goals(id),
    created_by UUID NOT NULL REFERENCES users(id),
    type VARCHAR(30) NOT NULL CHECK (type IN ('CHANGE_TARGET_AMOUNT', 'CHANGE_DEADLINE', 'CANCEL_GOAL')),
    proposed_value VARCHAR(255),
    justification VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'APPROVED', 'REJECTED', 'EXPIRED')),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_proposals_goal_id ON proposals (goal_id);
CREATE INDEX idx_proposals_status ON proposals (status);
CREATE INDEX idx_proposals_expires_at ON proposals (expires_at);
CREATE INDEX idx_proposals_created_at ON proposals (created_at);
CREATE UNIQUE INDEX uk_proposal_open_type ON proposals (goal_id, type) WHERE status = 'OPEN';

CREATE TABLE votes (
    id UUID PRIMARY KEY,
    proposal_id UUID NOT NULL REFERENCES proposals(id),
    user_id UUID NOT NULL REFERENCES users(id),
    choice VARCHAR(20) NOT NULL CHECK (choice IN ('APPROVE', 'REJECT')),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_vote_proposal_user UNIQUE (proposal_id, user_id)
);

CREATE INDEX idx_votes_proposal_id ON votes (proposal_id);
CREATE INDEX idx_votes_user_id ON votes (user_id);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(80) NOT NULL,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at);
