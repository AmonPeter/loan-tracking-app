package com.loan.app.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        return User.builder()
            .username(appUser.email())
            .password(appUser.passwordHash())
            .roles(appUser.role())
            .disabled(!appUser.enabled())
            .accountExpired(!appUser.accountNonExpired())
            .accountLocked(!appUser.accountNonLocked())
            .credentialsExpired(!appUser.credentialsNonExpired())
            .build();
    }
}
