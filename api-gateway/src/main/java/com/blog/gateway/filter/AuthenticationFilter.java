package com.blog.gateway.filter;

import com.blog.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // 1. Kiểm tra xem request có chứa Header Authorization không
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Thiếu Header Authorization");
            }

            // 2. Lấy token ra (Dạng: "Bearer eyJhbGci...")
            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7); // Cắt bỏ chữ "Bearer "
            }

            // 3. Validate Token
            try {
                jwtUtil.validateToken(authHeader);
            } catch (Exception e) {
                System.out.println("Token không hợp lệ!");
                throw new RuntimeException("Truy cập bị từ chối: Token không hợp lệ");
            }

            // 4. Nếu OK thì cho đi tiếp đến Service đích
            return chain.filter(exchange);
        });
    }

    public static class Config {
        // Class rỗng để config (bắt buộc phải có)
    }
}