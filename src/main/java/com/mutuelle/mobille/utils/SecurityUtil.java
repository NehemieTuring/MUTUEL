package com.mutuelle.mobille.utils;

import com.mutuelle.mobille.config.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static CustomUserDetails getCurrentCustomUserDetails() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Principal n'est pas une instance de CustomUserDetails");
        }

        return (CustomUserDetails) principal;
    }

    public static Long getCurrentUserRefId() {
        return getCurrentCustomUserDetails().getUserRefId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentCustomUserDetails().getEmail();
    }

    public static Long getCurrentAuthUserId() {
        return getCurrentCustomUserDetails().getId();
    }

    public static com.mutuelle.mobille.models.auth.AuthUser getCurrentAuthUser() {
        return getCurrentCustomUserDetails().getAuthUser();
    }
}