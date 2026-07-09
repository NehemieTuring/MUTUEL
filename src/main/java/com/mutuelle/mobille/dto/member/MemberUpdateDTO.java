package com.mutuelle.mobille.dto.member;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberUpdateDTO {

        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        private String firstname;

        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        private String lastname;

        @Pattern(
                regexp = "^(\\+2376|6)[0-9]{8}$",
                message = "Numéro invalide. Formats acceptés : 6XXXXXXXX ou +2376XXXXXXXX (9 chiffres après 6)"
        )
        @Size(min = 9, max = 13, message = "Le numéro doit contenir entre 9 et 13 caractères")
        private String phone;
}