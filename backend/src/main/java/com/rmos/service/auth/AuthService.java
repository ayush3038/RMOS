package com.rmos.service.auth;

import com.rmos.domain.auth.Role;
import com.rmos.domain.auth.User;
import com.rmos.domain.auth.SecurityAuditEvent;
import com.rmos.dto.auth.LoginRequest;
import com.rmos.dto.auth.LoginResponse;
import com.rmos.repository.auth.UserRepository;
import com.rmos.repository.auth.SecurityAuditEventRepository;
import com.rmos.security.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtils jwtUtils;
        private final SecurityAuditEventRepository auditEventRepository;

        public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
                        SecurityAuditEventRepository auditEventRepository) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtUtils = jwtUtils;
                this.auditEventRepository = auditEventRepository;
        }

        public LoginResponse login(LoginRequest request) {
                User user = userRepository.findByUsername(request.getUsername())
                                .orElse(null);

                if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                        auditEventRepository
                                        .save(new SecurityAuditEvent("LOGIN_FAILED", request.getUsername(),
                                                        "Invalid credentials"));
                        throw new org.springframework.security.authentication.BadCredentialsException(
                                        "Invalid username or password");
                }

                if (!user.isActive()) {
                        auditEventRepository
                                        .save(new SecurityAuditEvent("LOGIN_FAILED", request.getUsername(),
                                                        "Account deactivated"));
                        throw new org.springframework.security.authentication.DisabledException("Account is inactive");
                }

                String token = jwtUtils.generateToken(user.getUsername(), user.getRole());

                auditEventRepository
                                .save(new SecurityAuditEvent("LOGIN_SUCCESS", request.getUsername(),
                                                "User logged in successfully"));

                return new LoginResponse(token, jwtUtils.getExpirationSec(), user.getRole());
        }
}
