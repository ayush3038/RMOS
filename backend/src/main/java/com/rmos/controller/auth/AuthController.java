package com.rmos.controller.auth;

import com.rmos.dto.auth.LoginRequest;
import com.rmos.dto.auth.LoginResponse;
import com.rmos.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "UNAUTHORIZED", "Invalid username or password"));
        } catch (DisabledException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "UNAUTHORIZED", "Account is inactive"));
        }
    }

    // Quick inner class for stable API error format
    static class ErrorResponse {
        public String timestamp = java.time.Instant.now().toString();
        public int status;
        public String code;
        public String message;
        public java.util.List<String> details = java.util.Collections.emptyList();

        public ErrorResponse(int status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }
    }
}
