package com.finance.tracker.filter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        
        // 1️⃣ First, try to get token from Authorization header (for backward compatibility)
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }
        
        // 2️⃣ If not found, try to get token from HttpOnly cookie
        if (token == null) {
            token = getTokenFromCookie(request);
        }
        
        // 3️⃣ Validate and process the token
        if (token != null) {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                String sub = claims.getSubject();
                if (sub != null) {
                    UsernamePasswordAuthenticationToken a =
                            new UsernamePasswordAuthenticationToken(sub, null, java.util.List.of());
                    SecurityContextHolder.getContext().setAuthentication(a);
                }
            } catch (Exception e) {
                // ignore - let it be unauthenticated
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from HttpOnly cookie named "auth_token"
     */
    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
