package com.finance.tracker.filter;

import com.finance.tracker.service.impl.AccessTokenStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final AccessTokenStore accessTokenStore;

    public JwtAuthFilter(AccessTokenStore accessTokenStore) {
        this.accessTokenStore = accessTokenStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;

        // Bearer header (optional)
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }

        // If not present, try cookie named 'access_token'
        if (token == null) {
            token = getTokenFromCookie(request);
        }

        if (token != null && accessTokenStore.validate(token)) {
            String username = accessTokenStore.getUsername(token);
            if (username != null) {
                UsernamePasswordAuthenticationToken a = new UsernamePasswordAuthenticationToken(username, null, java.util.List.of());
                SecurityContextHolder.getContext().setAuthentication(a);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if ("access_token".equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
