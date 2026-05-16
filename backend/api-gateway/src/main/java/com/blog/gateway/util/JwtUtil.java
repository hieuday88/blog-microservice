package com.blog.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.Key;

@ApplicationScoped
public class JwtUtil {
    @ConfigProperty(name = "app.jwt-secret")
    String secret;

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
