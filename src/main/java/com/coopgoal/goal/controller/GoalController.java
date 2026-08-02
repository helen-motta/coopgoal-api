package com.coopgoal.goal.controller;

import com.coopgoal.contribution.service.ContributionService;
import com.coopgoal.goal.domain.GoalStatus;
import com.coopgoal.goal.dto.CreateGoalRequest;
import com.coopgoal.goal.dto.GoalProgressResponse;
import com.coopgoal.goal.dto.GoalResponse;
import com.coopgoal.goal.dto.GoalStatementResponse;
import com.coopgoal.goal.dto.UpdateGoalRequest;
import com.coopgoal.goal.service.GoalService;
import com.coopgoal.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GoalController {
    private final GoalService goalService;
    private final ContributionService contributionService;
    private final CurrentUserService currentUser;

    public GoalController(GoalService goalService, ContributionService contributionService,
                          CurrentUserService currentUser) {
        this.goalService = goalService;
        this.contributionService = contributionService;
        this.currentUser = currentUser;
    }

    @PostMapping("/groups/{groupId}/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@PathVariable UUID groupId, @Valid @RequestBody CreateGoalRequest request) {
        return GoalResponse.from(goalService.create(groupId, currentUser.id(), request));
    }

    @GetMapping("/groups/{groupId}/goals")
    public Page<GoalResponse> list(@PathVariable UUID groupId,
                                   @RequestParam(required = false) GoalStatus status,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,
                                   @RequestParam(required = false) String name,
                                   Pageable pageable) {
        return goalService.list(groupId, currentUser.id(), status, deadlineFrom, deadlineTo, name, pageable)
                .map(GoalResponse::from);
    }

    @GetMapping("/goals/{goalId}")
    public GoalResponse get(@PathVariable UUID goalId) {
        return GoalResponse.from(goalService.get(goalId, currentUser.id()));
    }

    @PatchMapping("/goals/{goalId}")
    public GoalResponse update(@PathVariable UUID goalId, @Valid @RequestBody UpdateGoalRequest request) {
        return GoalResponse.from(goalService.update(goalId, currentUser.id(), request));
    }

    @GetMapping("/goals/{goalId}/progress")
    public GoalProgressResponse progress(@PathVariable UUID goalId) {
        return goalService.progress(goalId, currentUser.id());
    }

    @GetMapping("/goals/{goalId}/statement")
    public GoalStatementResponse statement(@PathVariable UUID goalId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                            Pageable pageable) {
        return contributionService.statement(goalId, currentUser.id(), from, to, pageable);
    }
}
