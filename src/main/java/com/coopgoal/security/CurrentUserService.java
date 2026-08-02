package com.coopgoal.security;

import com.coopgoal.shared.exception.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {
    public UUID id() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthenticatedUser user) return user.id();
        throw new AccessDeniedException("Usuário não autenticado");
    }
}
