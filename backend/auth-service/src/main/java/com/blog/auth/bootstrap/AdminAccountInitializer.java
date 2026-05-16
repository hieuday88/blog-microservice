package com.blog.auth.bootstrap;

import com.blog.auth.entity.Role;
import com.blog.auth.entity.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AdminAccountInitializer {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_EMAIL = "admin@blog.local";

    @Transactional
    void onStart(@Observes StartupEvent event) {
        Role adminRole = Role.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.name = "ROLE_ADMIN";
            role.persist();
            return role;
        });

        Role userRole = Role.findByName("ROLE_USER").orElseGet(() -> {
            Role role = new Role();
            role.name = "ROLE_USER";
            role.persist();
            return role;
        });

        User.findByUsername(ADMIN_USERNAME).ifPresentOrElse(user -> {
            user.roles.add(adminRole);
        }, () -> {
            User user = new User();
            user.username = ADMIN_USERNAME;
            user.email = ADMIN_EMAIL;
            user.password = BcryptUtil.bcryptHash(ADMIN_PASSWORD);
            user.roles.add(adminRole);
            user.roles.add(userRole);
            user.persist();
        });
    }
}
