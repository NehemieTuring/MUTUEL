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

        @Pattern(regexp = "^(\\+237|0)[6-9][0-9]{8}$",
                message = "Format de téléphone invalide (ex: 0698765432 ou +237691234567)")
        @Size(max = 15)
        private String phone;
}