package com.coopgoal.auth.service;

import com.coopgoal.audit.service.AuditService;
import com.coopgoal.auth.dto.AuthResponse;
import com.coopgoal.auth.dto.LoginRequest;
import com.coopgoal.auth.dto.RegisterRequest;
import com.coopgoal.security.JwtService;
import com.coopgoal.shared.exception.BusinessRuleException;
import com.coopgoal.shared.exception.ResourceNotFoundException;
import com.coopgoal.user.domain.User;
import com.coopgoal.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessRuleException("USER_EMAIL_DUPLICATE", "Já existe uma conta com este e-mail");
        }
        User user = userRepository.save(User.create(request.name(), request.email(),
                passwordEncoder.encode(request.password())));
        auditService.record(user.getId(), "USER", user.getId(), "USER_REGISTERED", "Conta criada");
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuário não encontrado"));
        return response(user);
    }

    private AuthResponse response(User user) {
        String token = jwtService.generate(user.getId(), user.getEmail());
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), token, "Bearer",
                jwtService.expiration(token));
    }
}
