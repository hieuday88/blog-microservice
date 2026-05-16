package com.blog.auth.controller;

import com.blog.auth.entity.Role;
import com.blog.auth.entity.User;
import com.blog.auth.security.JwtTokenProvider;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Comparator;
import java.util.List;

@Path("/api/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {
    @Inject
    JwtTokenProvider jwtTokenProvider;

    @GET
    @Path("/me")
    public User getCurrentUser(@HeaderParam("Authorization") String authorization) {
        return currentUser(authorization);
    }

    @GET
    @Path("/{username}/profile")
    public User getUserProfile(@PathParam("username") String username) {
        return User.findByUsername(username)
                .orElseThrow(() -> new WebApplicationException("User not found: " + username, Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/checkUsernameAvailability")
    public boolean checkUsernameAvailability(@QueryParam("username") String username) {
        return !User.existsByUsername(username);
    }

    @GET
    public List<UserSummary> getUsers(@HeaderParam("Authorization") String authorization) {
        requireAdmin(authorization);
        return User.<User>listAll().stream()
                .sorted(Comparator.comparing(user -> user.id))
                .map(user -> new UserSummary(
                        user.id,
                        user.username,
                        user.email,
                        user.roles.stream().map(role -> role.name).sorted().toList()))
                .toList();
    }

    @PUT
    @Path("/changePassword")
    @Transactional
    public Response changePassword(@HeaderParam("Authorization") String authorization,
                                   com.blog.auth.dto.ChangePasswordDto dto) {
        User user = currentUser(authorization);
        if (!BcryptUtil.matches(dto.currentPassword, user.password)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"Mat khau hien tai khong dung\"}").build();
        }
        user.password = BcryptUtil.bcryptHash(dto.newPassword);
        return Response.ok("{\"message\": \"Doi mat khau thanh cong\"}").build();
    }

    @PUT
    @Path("/setOrUpdateInfo")
    @Transactional
    public User updateProfile(@HeaderParam("Authorization") String authorization, User userUpdate) {
        User currentUser = currentUser(authorization);
        if (userUpdate.email != null) {
            currentUser.email = userUpdate.email;
        }
        if (userUpdate.password != null) {
            currentUser.password = BcryptUtil.bcryptHash(userUpdate.password);
        }
        return currentUser;
    }

    @PUT
    @Path("/{username}/giveAdmin")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String giveAdmin(@HeaderParam("Authorization") String authorization, @PathParam("username") String username) {
        requireAdmin(authorization);
        User user = User.findByUsername(username)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
        Role adminRole = Role.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.name = "ROLE_ADMIN";
            role.persist();
            return role;
        });
        user.roles.add(adminRole);
        return "Granted ADMIN role to user: " + username;
    }

    @PUT
    @Path("/{username}/takeAdmin")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String takeAdmin(@HeaderParam("Authorization") String authorization, @PathParam("username") String username) {
        requireAdmin(authorization);
        if ("admin".equals(username)) {
            throw new WebApplicationException("Cannot remove admin role from default admin", Response.Status.BAD_REQUEST);
        }
        User user = User.findByUsername(username)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
        Role adminRole = Role.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new WebApplicationException("Role Admin not found", Response.Status.NOT_FOUND));
        user.roles.remove(adminRole);
        return "Removed ADMIN role from user: " + username;
    }

    @DELETE
    @Path("/{username}")
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteUser(@HeaderParam("Authorization") String authorization, @PathParam("username") String username) {
        requireAdmin(authorization);
        if ("admin".equals(username)) {
            throw new WebApplicationException("Cannot delete default admin", Response.Status.BAD_REQUEST);
        }
        User userToDelete = User.findByUsername(username)
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
        userToDelete.delete();
        return "Deleted user: " + username;
    }

    private void requireAdmin(String authorization) {
        User user = currentUser(authorization);
        boolean isAdmin = user.roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.name));
        if (!isAdmin) {
            throw new WebApplicationException("Admin role required", Response.Status.FORBIDDEN);
        }
    }

    private User currentUser(String authorization) {
        String token = bearerToken(authorization);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new WebApplicationException("Invalid token", Response.Status.UNAUTHORIZED);
        }
        return User.findByUsername(jwtTokenProvider.getUsername(token))
                .orElseThrow(() -> new WebApplicationException("User not found", Response.Status.NOT_FOUND));
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new WebApplicationException("Missing Authorization header", Response.Status.UNAUTHORIZED);
        }
        return authorization.substring(7);
    }

    public record UserSummary(Long id, String username, String email, List<String> roles) {
    }
}
