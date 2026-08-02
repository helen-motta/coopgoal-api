package com.coopgoal.proposal.controller;

import com.coopgoal.proposal.domain.ProposalStatus;
import com.coopgoal.proposal.dto.CreateProposalRequest;
import com.coopgoal.proposal.dto.ProposalResponse;
import com.coopgoal.proposal.dto.VoteRequest;
import com.coopgoal.proposal.dto.VoteResponse;
import com.coopgoal.proposal.service.ProposalService;
import com.coopgoal.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProposalController {
    private final ProposalService service;
    private final CurrentUserService currentUser;

    public ProposalController(ProposalService service, CurrentUserService currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping("/goals/{goalId}/proposals")
    @ResponseStatus(HttpStatus.CREATED)
    public ProposalResponse create(@PathVariable UUID goalId, @Valid @RequestBody CreateProposalRequest request) {
        var proposal = service.create(goalId, currentUser.id(), request);
        return ProposalResponse.from(proposal, service.votes(proposal.getId()).stream().map(VoteResponse::from).toList());
    }

    @GetMapping("/goals/{goalId}/proposals")
    public Page<ProposalResponse> list(@PathVariable UUID goalId,
                                       @RequestParam(required = false) ProposalStatus status,
                                       Pageable pageable) {
        return service.list(goalId, currentUser.id(), status, pageable)
                .map(p -> ProposalResponse.from(p, service.votes(p.getId()).stream().map(VoteResponse::from).toList()));
    }

    @GetMapping("/proposals/{proposalId}")
    public ProposalResponse get(@PathVariable UUID proposalId) {
        var proposal = service.get(proposalId, currentUser.id());
        return ProposalResponse.from(proposal, service.votes(proposalId).stream().map(VoteResponse::from).toList());
    }

    @PostMapping("/proposals/{proposalId}/votes")
    @ResponseStatus(HttpStatus.CREATED)
    public VoteResponse vote(@PathVariable UUID proposalId, @Valid @RequestBody VoteRequest request) {
        return VoteResponse.from(service.vote(proposalId, currentUser.id(), request.choice()));
    }
}
