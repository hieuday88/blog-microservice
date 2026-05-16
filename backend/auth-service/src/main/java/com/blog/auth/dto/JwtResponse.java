package com.blog.auth.dto;

public class JwtResponse {
    public String token;

    public JwtResponse(String token) {
        this.token = token;
    }
}
