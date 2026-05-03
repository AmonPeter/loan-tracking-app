package com.loan.app.user;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<AppUser, Long> {

    @Query("SELECT id, email, password_hash, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at, last_login_at FROM users WHERE lower(email) = lower(:email) LIMIT 1")
    Optional<AppUser> findByEmail(@Param("email") String email);
}
