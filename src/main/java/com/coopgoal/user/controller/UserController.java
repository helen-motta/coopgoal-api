package com.coopgoal.user.controller;

import com.coopgoal.contribution.dto.ContributionResponse;
import com.coopgoal.contribution.service.ContributionService;
import com.coopgoal.security.CurrentUserService;
import com.coopgoal.user.dto.DashboardResponse;
import com.coopgoal.user.dto.UserResponse;
import com.coopgoal.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserService userService;
    private final ContributionService contributionService;
    private final CurrentUserService currentUser;

    public UserController(UserService userService, ContributionService contributionService,
                          CurrentUserService currentUser) {
        this.userService = userService;
        this.contributionService = contributionService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public UserResponse me() { return UserResponse.from(userService.me(currentUser.id())); }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() { return userService.dashboard(currentUser.id()); }

    @GetMapping("/contributions")
    public Page<ContributionResponse> contributions(Pageable pageable) {
        return contributionService.listByUser(currentUser.id(), pageable).map(ContributionResponse::from);
    }
}
