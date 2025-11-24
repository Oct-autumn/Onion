package com.onion.onionserver.util;

import com.onion.onionserver.model.dao.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

@Component
public class JwtTools {
    // Should be auto-wired from application properties
    private final SecretKeySpec jwtKey;
    private final JwtParser jwtParser;
    private final long jwtExpiration; // Default to 1 hour

    public JwtTools(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.expiration:3600}") long jwtExpiration) {

        this.jwtKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        this.jwtParser = Jwts.parser().verifyWith(jwtKey).build();

        this.jwtExpiration = jwtExpiration;
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder().subject(String.valueOf(user.getId())).claim("role", user.getRole()).issuedAt(now).expiration(new Date(now.getTime() + jwtExpiration * 1000)).signWith(this.jwtKey).compact();
    }

    public Claims verifyToken(String token) {
        try {
            Claims claims = (Claims) jwtParser.parse(token).getPayload();
            if (claims.getExpiration().before(new Date())) return null; // Token expired
            return claims;
        } catch (Exception e) {
            return null; // Invalid token
        }
    }

    public static Integer getUserIdFromClaims(Claims claims) {
        return Integer.parseInt(claims.getSubject());
    }

    public static Long timeToExpiry(Claims claims) {
        return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
    }
}
