package com.mutuelle.mobille.dto;

import com.mutuelle.mobille.dto.assistance.TypeAssistanceResponseDto;
import com.mutuelle.mobille.dto.exercice.ExerciceResponseDTO;
import com.mutuelle.mobille.dto.member.MemberResponseDTO;
import com.mutuelle.mobille.dto.profile.AdminProfileDTO;
import com.mutuelle.mobille.dto.profile.MemberProfileDTO;
import com.mutuelle.mobille.dto.session.SessionResponseDTO;
import com.mutuelle.mobille.enums.Role;
import com.mutuelle.mobille.models.BorrowingCeilingInterval;
import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.models.account.AccountMutuelle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO principal qui fournit le contexte global de l'application
 * ainsi que les informations sur l'utilisateur connecté (si présent)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentContextResponseDTO {

    // ───────────────────────────────────────────────
    // Contexte applicatif général
    // ───────────────────────────────────────────────
    private SessionResponseDTO currentSession;
    private ExerciceResponseDTO currentExercice;
    private MutuelleConfig config;
    private List<TypeAssistanceResponseDto> typeAssistance;
    private List<BorrowingCeilingInterval> borrowingCeilingIntervals;

    // ───────────────────────────────────────────────
    // Informations d'authentification
    // ───────────────────────────────────────────────
    @Builder.Default
    private boolean authenticated = false;

    private String email;           // email de l'AuthUser
    private Long authUserId;           // ID dans la table auth_users
    private Role role;                 // ADMIN, MEMBER, ...
    private Long userRefId;            // ID du Member ou Admin lié

    // Raccourcis utiles pour le frontend
    @Builder.Default
    private boolean isAdmin = false;

    @Builder.Default
    private boolean isMember = false;

    // ───────────────────────────────────────────────
    // Profil de l'utilisateur connecté
    // Un seul des deux sera non-null selon le rôle
    // ───────────────────────────────────────────────
    private MemberResponseDTO memberProfile;
    private AdminProfileDTO adminProfile;


    // Statistiques MEMBRE (seulement si isMember = true)
    private Long memberTotalAssistances;       // nombre d'assistances reçues
    private Long memberTotalTransactions;      // toutes transactions du membre
    private Long totalMembers;

    // Statistiques GLOBALES ADMIN (seulement si isAdmin = true)
    private Long globalTotalAssistances;       // nombre total d'assistances (toutes sessions)
    private Long globalTotalTransactions;      // toutes transactions de la mutuelle
    private BigDecimal globalTotalAssistanceAmount;
    private AccountMutuelle accountMutuelle;
}