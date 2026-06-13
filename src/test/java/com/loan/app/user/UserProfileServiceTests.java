package com.loan.app.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserProfileServiceTests {

    @Test
    void changePasswordStoresEncodedNewPasswordForSignedInUser() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository(user());

        new UserProfileService(userRepository, new TestPasswordEncoder())
            .changePassword("user@example.com", "Current123", "NewSecret123", "NewSecret123");

        assertEquals(10L, userRepository.saved.id());
        assertEquals("encoded:NewSecret123", userRepository.saved.passwordHash());
        assertEquals("user@example.com", userRepository.saved.email());
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository(user());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new UserProfileService(userRepository, new TestPasswordEncoder())
                .changePassword("user@example.com", "Wrong1234", "NewSecret123", "NewSecret123")
        );

        assertEquals("Current password is incorrect.", ex.getMessage());
        assertEquals(null, userRepository.saved);
    }

    @Test
    void changePasswordRejectsConfirmationMismatch() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository(user());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new UserProfileService(userRepository, new TestPasswordEncoder())
                .changePassword("user@example.com", "Current123", "NewSecret123", "Different123")
        );

        assertEquals("New password confirmation does not match.", ex.getMessage());
        assertEquals(null, userRepository.saved);
    }

    private AppUser user() {
        return new AppUser(
            10L,
            "user@example.com",
            "encoded:Current123",
            "USER",
            true,
            true,
            true,
            true,
            OffsetDateTime.parse("2026-01-01T00:00:00Z"),
            OffsetDateTime.parse("2026-01-01T00:00:00Z"),
            null
        );
    }

    private static class TestPasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return "encoded:" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encode(rawPassword).equals(encodedPassword);
        }
    }

    private static class InMemoryUserRepository implements UserRepository {

        private final AppUser user;
        private AppUser saved;

        private InMemoryUserRepository(AppUser user) {
            this.user = user;
        }

        @Override
        public Optional<AppUser> findByEmail(String email) {
            return user.email().equalsIgnoreCase(email) ? Optional.of(user) : Optional.empty();
        }

        @Override
        public <S extends AppUser> S save(S entity) {
            saved = entity;
            return entity;
        }

        @Override
        public <S extends AppUser> Iterable<S> saveAll(Iterable<S> entities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<AppUser> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Long id) {
            return false;
        }

        @Override
        public Iterable<AppUser> findAll() {
            return List.of();
        }

        @Override
        public Iterable<AppUser> findAllById(Iterable<Long> longs) {
            return List.of();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(AppUser entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll(Iterable<? extends AppUser> entities) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }
    }
}
