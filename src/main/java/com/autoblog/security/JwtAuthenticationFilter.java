package com.autoblog.security;

import com.autoblog.identity.domain.UserAccountStatus;
import com.autoblog.identity.infrastructure.UserAccountJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserAccountJpaRepository users;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            UserAccountJpaRepository users,
            AuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtTokenService = jwtTokenService;
        this.users = users;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!authorization.startsWith(BEARER_PREFIX)) {
            authenticationEntryPoint.commence(request, response, new InvalidJwtException("Invalid access token", null));
            return;
        }

        try {
            JwtClaims claims = jwtTokenService.validate(authorization.substring(BEARER_PREFIX.length()));
            users.findById(claims.userId())
                    .filter(user -> user.getStatus() == UserAccountStatus.ACTIVE)
                    .ifPresent(user -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    new CurrentUserPrincipal(user.getId(), user.getEmail()),
                                    null,
                                    java.util.List.of()
                            )
                    ));
        } catch (InvalidJwtException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, exception);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
