package com.coopgoal.contribution.controller;

import com.coopgoal.contribution.dto.ContributionResponse;
import com.coopgoal.contribution.dto.CreateContributionRequest;
import com.coopgoal.contribution.service.ContributionService;
import com.coopgoal.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/goals/{goalId}/contributions")
public class ContributionController {
    private final ContributionService contributionService;
    private final CurrentUserService currentUser;

    public ContributionController(ContributionService contributionService, CurrentUserService currentUser) {
        this.contributionService = contributionService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContributionResponse create(@PathVariable UUID goalId,
                                       @RequestHeader(name = "Idempotency-Key", required = false) String key,
                                       @Valid @RequestBody CreateContributionRequest request) {
        return ContributionResponse.from(contributionService.register(goalId, currentUser.id(),
                request.amount(), request.description(), key));
    }

    @GetMapping
    public Page<ContributionResponse> list(@PathVariable UUID goalId,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                           Pageable pageable) {
        return contributionService.listByGoal(goalId, currentUser.id(), from, to, pageable)
                .map(ContributionResponse::from);
    }
}
