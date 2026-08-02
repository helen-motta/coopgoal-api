package com.coopgoal.proposal.service;

import com.coopgoal.proposal.domain.Proposal;
import com.coopgoal.proposal.domain.ProposalStatus;
import com.coopgoal.proposal.repository.ProposalRepository;
import com.coopgoal.audit.service.AuditService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
public class ProposalExpirationScheduler {
    private final ProposalRepository repository;
    private final Clock clock;
    private final AuditService auditService;

    public ProposalExpirationScheduler(ProposalRepository repository, Clock clock, AuditService auditService) {
        this.repository = repository;
        this.clock = clock;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelayString = "PT15M")
    @Transactional
    public void expireOpenProposals() {
        for (Proposal proposal : repository.findAllByStatusAndExpiresAtBefore(ProposalStatus.OPEN, Instant.now(clock))) {
            proposal.expire();
            auditService.record(null, "PROPOSAL", proposal.getId(), "PROPOSAL_EXPIRED", "Expiração agendada");
        }
    }
}
