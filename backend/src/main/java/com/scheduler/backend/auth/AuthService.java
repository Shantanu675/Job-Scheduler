package com.scheduler.backend.auth;

import com.scheduler.backend.auth.dto.AuthResponse;
import com.scheduler.backend.auth.dto.LoginRequest;
import com.scheduler.backend.auth.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        User user = new User();

        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setName(request.getName().trim());

        // Never store the plain-text password.
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole("USER");

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = authenticate(request);

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getOrganizationId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public User authenticate(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        return user;
    }
}