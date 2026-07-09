package com.mutuelle.mobille.enums;

public enum MemberStatus {
    PENDING,        // Inscription non payée — membre en attente d'entrée dans la mutuelle
    ACTIF,          // En règle (inscription payée, pas de dette de solidarité ni renflouement)
    INSOLVABLE,     // Non en règle mais dette < seuil
    INACTIF         // Non en règle et dette >= seuil
}