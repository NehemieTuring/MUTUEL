package com.mutuelle.mobille.models.auth;

import com.mutuelle.mobille.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "pin", nullable = false)
    private String pin = "2025";

    // Référence vers l'entité réelle (Member ou Admin)
    @Column(name = "user_ref_id", nullable = false)
    private Long userRefId;
}
