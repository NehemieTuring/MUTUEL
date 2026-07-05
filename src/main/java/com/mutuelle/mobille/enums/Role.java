package com.mutuelle.mobille.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
    SUPER_ADMIN("SUPER_ADMIN"),
    ADMIN("ADMIN"),
    PRESIDENT("PRESIDENT"),
    TRESORIER("TRESORIER"),
    MEMBER("MEMBER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Très important : Spring Security attend "ROLE_XXX"
    public SimpleGrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + value);
    }

    // Pour convertir une String → Role (utile dans le JWT filter)
    public static Role fromValue(String value) {
        for (Role role : values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle inconnu : " + value);
    }
}