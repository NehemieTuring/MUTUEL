package com.mutuelle.mobille.enums;

public enum MemberStatus {
    PENDING,        // Non inscrit — complément d'inscription non payé
    ACTIF,          // À jour — inscription soldée, pas de dette solidarité ni renflouement
    NON_A_JOUR,     // Non à jour — dette solidarité et/ou renflouement < seuil inactivité
    INACTIF         // Inactif — dette solidarité + renflouement >= seuil (250 000 FCFA)
}
