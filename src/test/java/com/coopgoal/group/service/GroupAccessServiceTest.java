package com.coopgoal.group.service;

import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.repository.MembershipRepository;
import com.coopgoal.shared.exception.AccessDeniedException;
import com.coopgoal.user.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupAccessServiceTest {
    @Test
    void rejectsMemberWhenOwnerOrAdminRoleIsRequired() {
        MembershipRepository repository = mock(MembershipRepository.class);
        GroupAccessService service = new GroupAccessService(repository);
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.create("Membro", "member@example.com", "encoded");
        CoopGroup group = CoopGroup.create("Grupo", null, user);
        when(repository.findByGroupIdAndUserId(groupId, userId))
                .thenReturn(Optional.of(Membership.create(group, user, MembershipRole.MEMBER)));

        assertThatThrownBy(() -> service.requireRole(groupId, userId,
                MembershipRole.OWNER, MembershipRole.ADMIN))
                .isInstanceOf(AccessDeniedException.class);
    }
}
