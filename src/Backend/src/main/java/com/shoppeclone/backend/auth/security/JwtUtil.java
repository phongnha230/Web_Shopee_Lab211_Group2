package com.shoppeclone.backend.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateAccessToken(String email) {
        return Jwts.builder()
            .subject(email)  // ✅ Dùng subject() thay vì setSubject()
            .issuedAt(new Date())  // ✅ Dùng issuedAt() thay vì setIssuedAt()
            .expiration(new Date(System.currentTimeMillis() + expiration))  // ✅ Dùng expiration()
            .signWith(getSignKey(), Jwts.SIG.HS256)  // ✅ Dùng Jwts.SIG.HS256
            .compact();
    }
    
    public String generateRefreshToken(String email) {
        return Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(getSignKey(), Jwts.SIG.HS256)
            .compact();
    }
    
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private Claims extractClaims(String token) {
        return Jwts.parser()  // ✅ Dùng parser() thay vì parserBuilder()
            .verifyWith(getSignKey())  // ✅ Dùng verifyWith() thay vì setSigningKey()
            .build()
            .parseSignedClaims(token)  // ✅ Dùng parseSignedClaims() thay vì parseClaimsJws()
            .getPayload();  // ✅ Dùng getPayload() thay vì getBody()
    }
}