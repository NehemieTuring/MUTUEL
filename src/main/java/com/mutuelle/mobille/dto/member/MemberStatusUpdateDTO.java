package com.mutuelle.mobille.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberStatusUpdateDTO {

    @NotNull(message = "Le statut actif est obligatoire")
    private Boolean active;
}