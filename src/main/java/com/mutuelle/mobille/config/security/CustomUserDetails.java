package com.mutuelle.mobille.config.security;

import com.mutuelle.mobille.models.auth.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final AuthUser authUser;

    public CustomUserDetails(AuthUser authUser) {
        this.authUser = authUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = "ROLE_" + authUser.getRole().name();
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return authUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return authUser.getEmail(); // ou authUser.getId().toString() si vous préférez
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // Méthodes utilitaires pour accéder aux données personnalisées
    public AuthUser getAuthUser() {
        return authUser;
    }

    public Long getUserRefId() {
        return authUser.getUserRefId();
    }

    public Long getId() {
        return authUser.getId();
    }

    public String getEmail() {
        return authUser.getEmail();
    }
}