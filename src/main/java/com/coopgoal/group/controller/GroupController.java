package com.coopgoal.group.controller;

import com.coopgoal.group.dto.AddMemberRequest;
import com.coopgoal.group.dto.CreateGroupRequest;
import com.coopgoal.group.dto.GroupResponse;
import com.coopgoal.group.dto.MembershipResponse;
import com.coopgoal.group.dto.UpdateGroupRequest;
import com.coopgoal.group.service.GroupService;
import com.coopgoal.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;
    private final CurrentUserService currentUser;

    public GroupController(GroupService groupService, CurrentUserService currentUser) {
        this.groupService = groupService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(@Valid @RequestBody CreateGroupRequest request) {
        return GroupResponse.from(groupService.create(currentUser.id(), request));
    }

    @GetMapping
    public Page<GroupResponse> list(Pageable pageable) {
        return groupService.list(currentUser.id(), pageable).map(GroupResponse::from);
    }

    @GetMapping("/{groupId}")
    public GroupResponse get(@PathVariable UUID groupId) {
        return GroupResponse.from(groupService.get(groupId, currentUser.id()));
    }

    @PatchMapping("/{groupId}")
    public GroupResponse update(@PathVariable UUID groupId, @Valid @RequestBody UpdateGroupRequest request) {
        return GroupResponse.from(groupService.update(groupId, currentUser.id(), request));
    }

    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipResponse addMember(@PathVariable UUID groupId, @Valid @RequestBody AddMemberRequest request) {
        return MembershipResponse.from(groupService.addMember(groupId, currentUser.id(), request));
    }

    @GetMapping("/{groupId}/members")
    public List<MembershipResponse> members(@PathVariable UUID groupId) {
        return groupService.listMembers(groupId, currentUser.id()).stream().map(MembershipResponse::from).toList();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID groupId, @PathVariable UUID userId) {
        groupService.removeMember(groupId, userId, currentUser.id());
    }
}
