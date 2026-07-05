package com.mutuelle.mobille.dto.member;

import com.mutuelle.mobille.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberMutuelleStatusUpdateDTO {

    @NotNull(message = "Le statut est obligatoire (ACTIF, INSOLVABLE, INACTIF)")
    private MemberStatus status;
}
