package com.onion.onionserver.util;

import com.onion.onionserver.manager.UserManager;
import com.onion.onionserver.model.dao.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * JWT Filter to authenticate requests based on JWT tokens.
 * This filter extracts the JWT token from the Authorization header,
 * validates it, and sets the authentication in the security context.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTools jwtTools;
    private final UserManager userManager;
    private final String BEARER_PREFIX;

    @Autowired
    JwtFilter(JwtTools jwtTools, UserManager userManager, @Value("${auth.prefix}") String bearerPrefix) {
        this.jwtTools = jwtTools;
        this.userManager = userManager;
        this.BEARER_PREFIX = bearerPrefix + " ";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            Claims claims = jwtTools.verifyToken(token);
            if (claims != null) {
                // Token is valid, set user in UserManager
                Integer userId = JwtTools.getUserIdFromClaims(claims);
                User user = userManager.getUserById(userId);
                if (user != null) {
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities()
                    ));
                } else {
                    // User not found, clear any existing authentication
                    SecurityContextHolder.clearContext();
                }
            } else {
                // Invalid token, clear any existing authentication
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
