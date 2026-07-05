package com.mutuelle.mobille.enums;

public enum MemberStatus {
    ACTIF,          // En règle (pas de dette de solidarité ni renflouement)
    INSOLVABLE,     // Non en règle mais dette < 250 000
    INACTIF         // Non en règle et dette >= 250 000
}