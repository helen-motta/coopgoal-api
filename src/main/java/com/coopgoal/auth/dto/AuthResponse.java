package com.coopgoal.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(UUID userId, String name, String email, String accessToken,
                           String tokenType, Instant expiresAt) {
}
