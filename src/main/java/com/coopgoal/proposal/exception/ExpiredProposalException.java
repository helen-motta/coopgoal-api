package com.coopgoal.proposal.exception;

import com.coopgoal.shared.exception.BusinessRuleException;

public class ExpiredProposalException extends BusinessRuleException {
    public ExpiredProposalException() {
        super("PROPOSAL_EXPIRED", "A proposta expirou e não pode receber votos");
    }
}
