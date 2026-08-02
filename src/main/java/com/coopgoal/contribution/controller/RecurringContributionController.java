package com.coopgoal.contribution.controller;

import com.coopgoal.contribution.dto.CreateRecurringContributionRequest;
import com.coopgoal.contribution.dto.RecurringContributionResponse;
import com.coopgoal.contribution.dto.UpdateRecurringContributionRequest;
import com.coopgoal.contribution.service.RecurringContributionService;
import com.coopgoal.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RecurringContributionController {
    private final RecurringContributionService service;
    private final CurrentUserService currentUser;

    public RecurringContributionController(RecurringContributionService service, CurrentUserService currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping("/goals/{goalId}/recurring-contributions")
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringContributionResponse create(@PathVariable UUID goalId,
                                                 @Valid @RequestBody CreateRecurringContributionRequest request) {
        return RecurringContributionResponse.from(service.create(goalId, currentUser.id(), request));
    }

    @GetMapping("/goals/{goalId}/recurring-contributions")
    public List<RecurringContributionResponse> list(@PathVariable UUID goalId) {
        return service.list(goalId, currentUser.id()).stream().map(RecurringContributionResponse::from).toList();
    }

    @PatchMapping("/recurring-contributions/{id}")
    public RecurringContributionResponse update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateRecurringContributionRequest request) {
        return RecurringContributionResponse.from(service.update(id, currentUser.id(), request));
    }

    @DeleteMapping("/recurring-contributions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id, currentUser.id()); }
}
