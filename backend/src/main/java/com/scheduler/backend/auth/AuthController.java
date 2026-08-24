package com.scheduler.backend.auth;

import com.scheduler.backend.auth.dto.AuthResponse;
import com.scheduler.backend.auth.dto.LoginRequest;
import com.scheduler.backend.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = authService.register(request);

        return new AuthResponse(
                null,
                user.getId(),
                user.getOrganizationId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}