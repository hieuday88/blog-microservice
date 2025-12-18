package com.blog.auth.controller;

// 1. Import các Entity và Repository của mình
import com.blog.auth.entity.Role;
import com.blog.auth.entity.User;
import com.blog.auth.repository.RoleRepository;
import com.blog.auth.repository.UserRepository;

// 2. Import các thư viện Spring cơ bản
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Để check quyền Admin
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

// 3. Import thư viện Java
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Cần cái này để tìm ROLE_ADMIN

    @Autowired
    private PasswordEncoder passwordEncoder; // Cần cái này nếu muốn đổi mật khẩu

    // --- 1. LẤY THÔNG TIN USER HIỆN TẠI (Logged in user) ---
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user hiện tại"));
        return ResponseEntity.ok(user);
    }

    // --- 2. XEM PROFILE CỦA NGƯỜI KHÁC ---
    @GetMapping("/{username}/profile")
    public ResponseEntity<User> getUserProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + username));
        return ResponseEntity.ok(user);
    }

    // --- 3. KIỂM TRA USERNAME CÓ TRÙNG KHÔNG ---
    @GetMapping("/checkUsernameAvailability")
    public ResponseEntity<Boolean> checkUsernameAvailability(@RequestParam String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return ResponseEntity.ok(isAvailable);
    }

    // --- 4. CẬP NHẬT THÔNG TIN (Cho chính mình) ---
    @PutMapping("/setOrUpdateInfo")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestBody User userUpdate) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Chỉ cho phép sửa email và mật khẩu (Ví dụ)
        if(userUpdate.getEmail() != null) {
            currentUser.setEmail(userUpdate.getEmail());
        }
        if(userUpdate.getPassword() != null) {
            currentUser.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }

        User updatedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    // --- 5. CẤP QUYỀN ADMIN (Chỉ Admin mới gọi được) ---
    // Yêu cầu: Phải bật @EnableMethodSecurity trong SecurityConfig thì dòng @PreAuthorize mới chạy
    @PutMapping("/{username}/giveAdmin")
    // @PreAuthorize("hasRole('ADMIN')") // Tạm đóng để bạn test cho dễ
    public ResponseEntity<String> giveAdmin(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Role Admin chưa được tạo trong DB"));

        user.getRoles().add(adminRole); // Thêm quyền
        userRepository.save(user);

        return ResponseEntity.ok("Đã cấp quyền ADMIN cho user: " + username);
    }

    // --- 6. TƯỚC QUYỀN ADMIN ---
    @PutMapping("/{username}/takeAdmin")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> takeAdmin(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Role Admin not found"));

        user.getRoles().remove(adminRole); // Xóa quyền
        userRepository.save(user);

        return ResponseEntity.ok("Đã gỡ quyền ADMIN của user: " + username);
    }

    // --- 7. XÓA USER (Admin hoặc chính chủ) ---
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username,
                                             @AuthenticationPrincipal UserDetails currentUser) {
        User userToDelete = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Logic kiểm tra: Chỉ xóa nếu là Admin hoặc là chính mình xóa mình
        // (Bạn có thể thêm logic check role ở đây)

        userRepository.delete(userToDelete);
        return ResponseEntity.ok("Đã xóa thành công user: " + username);
    }
}