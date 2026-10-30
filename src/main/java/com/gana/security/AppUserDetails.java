package com.gana.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * The authenticated principal: Spring Security's User plus the account's
 * current token version, which issued JWTs are checked against.
 */
public class AppUserDetails extends User {

    private final int tokenVersion;

    public AppUserDetails(com.gana.entity.User user) {
        super(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        this.tokenVersion = user.getTokenVersion();
    }

    public int getTokenVersion() {
        return tokenVersion;
    }
}
