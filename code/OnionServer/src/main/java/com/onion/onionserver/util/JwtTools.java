package com.onion.onionserver.util;

import com.onion.onionserver.model.dao.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTools {
    // Should be auto-wired from application properties
    private final String jwtSecret;
    private final long jwtExpiration; // Default to 1 hour

    public JwtTools(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration:3600}") long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtExpiration * 1000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public Claims verifyToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            if (claims.getExpiration().before(new Date()))
                return null; // Token expired
            return claims;
        } catch (Exception e) {
            return null; // Invalid token
        }
    }

    public static Long getUserIdFromClaims(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public static Long timeToExpiry(Claims claims) {
        return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
    }
}
