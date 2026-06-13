package com.loan.app.user;

import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Signed-in user could not be resolved.");
        }
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required.");
        }
        validatePassword(newPassword);
        if (confirmPassword == null || !confirmPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password confirmation does not match.");
        }

        AppUser user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Signed-in user could not be found."));

        if (!passwordEncoder.matches(currentPassword, user.passwordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, user.passwordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password.");
        }

        userRepository.save(new AppUser(
            user.id(),
            user.email(),
            passwordEncoder.encode(newPassword),
            user.role(),
            user.enabled(),
            user.accountNonExpired(),
            user.accountNonLocked(),
            user.credentialsNonExpired(),
            user.createdAt(),
            OffsetDateTime.now(),
            user.lastLoginAt()
        ));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }
}
