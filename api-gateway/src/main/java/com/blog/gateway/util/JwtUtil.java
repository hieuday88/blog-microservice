package com.blog.gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;

@Component
public class JwtUtil {

    // COPY Y HỆT SECRET KEY BÊN AUTH SERVICE SANG ĐÂY
    public static final String SECRET = "DayLaKhoaBiMatCuaNhom8RatDaiVaKhoDoanDeBaoMatToken1234567890";

    public void validateToken(String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}