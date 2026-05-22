package com.hospital.config;

import com.hospital.entity.Role;
import com.hospital.entity.User;
import com.hospital.repository.RoleRepository;
import com.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Tạo tài khoản mẫu khi hệ thống chạy lần đầu.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN", "Quản trị toàn hệ thống")));
        Role nhanVienRole = roleRepository.findByName("ROLE_NHANVIEN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_NHANVIEN", "Nhân viên nghiệp vụ")));
        Role thuKhoRole = roleRepository.findByName("ROLE_THUKHO")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_THUKHO", "Thủ kho")));
        Role keToanRole = roleRepository.findByName("ROLE_KETOAN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_KETOAN", "Kế toán")));

        User admin = userRepository.findByUsername("admin").orElseGet(User::new);
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setFullName("Quản trị viên hệ thống");
        admin.setEmail("admin@hospital.com");
        admin.setEnabled(true);
        admin.setRoles(new HashSet<>(Set.of(adminRole)));
        userRepository.save(admin);

        User staff = userRepository.findByUsername("staff").orElseGet(User::new);
        staff.setUsername("staff");
        staff.setPassword(passwordEncoder.encode("123456"));
        staff.setFullName("Nhân viên kho");
        staff.setEmail("staff@hospital.com");
        staff.setEnabled(true);
        staff.setRoles(new HashSet<>(Set.of(nhanVienRole, thuKhoRole)));
        userRepository.save(staff);

        User accountant = userRepository.findByUsername("ketoan").orElseGet(User::new);
        accountant.setUsername("ketoan");
        accountant.setPassword(passwordEncoder.encode("123456"));
        accountant.setFullName("Kế toán kho");
        accountant.setEmail("ketoan@hospital.com");
        accountant.setEnabled(true);
        accountant.setRoles(new HashSet<>(Set.of(keToanRole)));
        userRepository.save(accountant);
    }
}
