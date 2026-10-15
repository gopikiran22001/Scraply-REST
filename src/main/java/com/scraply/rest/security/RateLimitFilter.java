package com.scraply.rest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Value("${scraply.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${scraply.rate-limit.login.limit:5}")
    private int loginLimit;

    @Value("${scraply.rate-limit.login.window:60s}")
    private Duration loginWindow;

    @Value("${scraply.rate-limit.register.limit:3}")
    private int registerLimit;

    @Value("${scraply.rate-limit.register.window:1h}")
    private Duration registerWindow;

    @Value("${scraply.rate-limit.authenticated.limit:100}")
    private int authenticatedLimit;

    @Value("${scraply.rate-limit.authenticated.window:60s}")
    private Duration authenticatedWindow;

    private final AntPathRequestMatcher loginMatcher = new AntPathRequestMatcher("/auth/login", "POST");
    private final AntPathRequestMatcher registerMatcher = new AntPathRequestMatcher("/auth/register", "POST");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Policy policy = policyFor(request);
        RateLimitService.Result result;
        try {
            result = rateLimitService.check(policy.category(), clientKey(request), policy.limit(), policy.window());
        } catch (RuntimeException exception) {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Rate limiting service is temporarily unavailable");
            return;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

        if (!result.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(Math.max(1, result.retryAfterSeconds())));
            response.setContentType("application/json");
            response.getOutputStream().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}"
                            .getBytes(StandardCharsets.UTF_8));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Policy policyFor(HttpServletRequest request) {
        if (loginMatcher.matches(request)) {
            return new Policy("login", loginLimit, loginWindow);
        }
        if (registerMatcher.matches(request)) {
            return new Policy("register", registerLimit, registerWindow);
        }
        return new Policy("api", authenticatedLimit, authenticatedWindow);
    }

    private String clientKey(HttpServletRequest request) {
        String address = request.getRemoteAddr();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(address.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash rate-limit client address", exception);
        }
    }

    private record Policy(String category, int limit, Duration window) {
    }
}
