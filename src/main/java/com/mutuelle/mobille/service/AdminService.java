package com.mutuelle.mobille.service;

import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.auth.AuthUser;
import  com.mutuelle.mobille.repository.AuthUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    private final AuthUserRepository authUserRepository;

    public AdminService(AuthUserRepository authUserRepository){
        this.authUserRepository=authUserRepository;
    }

    public AuthUser getAuthAdmin() {
        return authUserRepository.findFirstByRole(Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("Aucun administrateur trouvé"));
    }
}
