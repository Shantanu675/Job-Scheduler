package com.scheduler.backend.config;

import com.scheduler.backend.auth.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");
        System.out.println(">>> JWT FILTER CALLED: " + request.getRequestURI());
        System.out.println(">>> Authorization: " + authorization);
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorization.substring(7);

        try {

            Claims claims =
                    jwtService.extractClaims(token);

            Long userId =
                    Long.valueOf(claims.getSubject());

            String role =
                    claims.get("role", String.class);

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                List<SimpleGrantedAuthority> authorities =
                        role == null
                                ? List.of()
                                : List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
