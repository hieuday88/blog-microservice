package com.blog.auth.controller;

import com.blog.auth.dto.JwtResponse;
import com.blog.auth.dto.LoginDto;
import com.blog.auth.dto.RegisterDto;
import com.blog.auth.entity.Role;
import com.blog.auth.entity.User;
import com.blog.auth.security.JwtTokenProvider;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {
    @Inject
    JwtTokenProvider jwtTokenProvider;

    @POST
    @Path("/login")
    public Response login(LoginDto loginDto) {
        try {
            User user = User.findByUsername(loginDto.username)
                    .orElseThrow(() -> new WebApplicationException("Invalid username or password", Response.Status.UNAUTHORIZED));

            if (!BcryptUtil.matches(loginDto.password, user.password)) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid username or password").build();
            }

            String token = jwtTokenProvider.generateToken(user);
            return Response.ok(new JwtResponse(token)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Loi login: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterDto registerDto) {
        try {
            if (User.existsByUsername(registerDto.username)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Username already exists").build();
            }

            Role userRole = Role.findByName("ROLE_USER").orElseGet(() -> {
                Role role = new Role();
                role.name = "ROLE_USER";
                role.persist();
                return role;
            });

            User user = new User();
            user.username = registerDto.username;
            user.email = registerDto.email;
            user.password = BcryptUtil.bcryptHash(registerDto.password);
            user.roles.add(userRole);
            user.persist();

            return Response.ok("{\"message\": \"Register successfully\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Loi register: " + e.getMessage() + "\"}").build();
        }
    }
}
