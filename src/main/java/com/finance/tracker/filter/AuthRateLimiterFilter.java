package com.finance.tracker.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthRateLimiterFilter extends OncePerRequestFilter {

    private static final int MAX_REQUEST = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        RequestWindow window = windows.compute(key, (ignored, current) -> resetIfExpired(current));

        if (window.count.incrementAndGet() > MAX_REQUEST) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"message\",\"Too many authentication requests\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private RequestWindow resetIfExpired(RequestWindow current) {
        if (current == null || Instant.now().isAfter(current.startAt.plus(WINDOW))){
            return new RequestWindow(Instant.now());
        }

        return current;
    }

    private record RequestWindow(Instant startAt, AtomicInteger count) {
        private RequestWindow(Instant startAt) {
            this(startAt, new AtomicInteger(0));
        }
    }
}
