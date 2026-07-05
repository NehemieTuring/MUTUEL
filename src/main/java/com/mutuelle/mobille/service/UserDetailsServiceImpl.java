package com.mutuelle.mobille.service;

import com.mutuelle.mobille.config.security.CustomUserDetails;
import com.mutuelle.mobille.models.auth.AuthUser;
import com.mutuelle.mobille.repository.AuthUserRepository;
import com.mutuelle.mobille.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final JwtUtils jwtUtils;

    @Override
    public UserDetails loadUserByUsername(String identifier) {
        System.out.println("loadUserByUsername appelé avec : " + identifier);

        AuthUser authUser = authUserRepository.findByEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + identifier));

        return new CustomUserDetails(authUser);
    }
}