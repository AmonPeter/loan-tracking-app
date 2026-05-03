package com.loan.app.user;

public record AdminUserView(
    Long id,
    String email,
    String role,
    boolean enabled
) {
}
