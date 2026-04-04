package com.movie.movie_booking_api.config;

import com.movie.movie_booking_api.entity.User;
import com.movie.movie_booking_api.entity.UserRole;
import com.movie.movie_booking_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@gmail.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name("admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("123"))
                    .fullName("Administrator")
                    .legacyName("admin")
                    .role(UserRole.ADMIN)
                    .locked(false)
                    .emailVerified(true)
                    .emailVerifiedAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            log.info("Admin account created: {} / 123", adminEmail);
        } else {
            log.info("Admin account already exists: {}", adminEmail);
        }

        String[] testUsers = {
                "huy123@gmail.com", "dat123@gmail.com", "hao123@gmail.com",
                "duan123@gmail.com", "anh123@gmail.com", "trinh123@gmail.com",
                "viet123@gmail.com"
        };

        for (String email : testUsers) {
            if (!userRepository.existsByEmail(email)) {
                String name = email.split("@")[0];
                User u = User.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode("123"))
                        .fullName(name.substring(0, 1).toUpperCase() + name.substring(1))
                        .legacyName(name)
                        .role(UserRole.CUSTOMER)
                        .locked(false)
                        .emailVerified(true)
                        .emailVerifiedAt(LocalDateTime.now())
                        .build();
                userRepository.save(u);
                log.info("Test account created: {} / 123", email);
            }
        }
    }
}
