package com.blog.auth.security;

import com.blog.auth.entity.Role;
import com.blog.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.Key;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class JwtTokenProvider {
    @ConfigProperty(name = "app.jwt-secret")
    String jwtSecret;

    @ConfigProperty(name = "app.jwt-expiration-milliseconds")
    long jwtExpirationDate;

    public String generateToken(User user) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);
        List<String> roles = user.roles.stream().map(role -> role.name).toList();

        return Jwts.builder()
                .setSubject(user.username)
                .claim("uid", user.id)
                .claim("roles", roles)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(key())
                .compact();
    }

    public String getUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            claims(token);
            return true;
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
