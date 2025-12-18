package com.blog.auth.security;

import com.blog.auth.entity.User;
import com.blog.auth.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // 1. Tìm user trong DB bằng username hoặc email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        // 2. Chuyển đổi Role của Entity sang GrantedAuthority của Spring Security
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        // 3. Trả về đối tượng User chuẩn của Spring Security (không phải User entity của mình)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}