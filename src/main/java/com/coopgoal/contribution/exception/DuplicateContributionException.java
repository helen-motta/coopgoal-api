package com.coopgoal.contribution.exception;

import com.coopgoal.shared.exception.BusinessRuleException;

public class DuplicateContributionException extends BusinessRuleException {
    public DuplicateContributionException() {
        super("CONTRIBUTION_DUPLICATE", "Já existe uma contribuição com esta chave de idempotência");
    }
}
