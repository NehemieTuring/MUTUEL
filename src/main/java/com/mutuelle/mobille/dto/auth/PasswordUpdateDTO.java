package com.mutuelle.mobille.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateDTO {
    @NotBlank(message = "Mot de passe actuel requis")
    private String currentPassword;

    @NotBlank @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
    private String newPassword;

    @NotBlank
    private String confirmNewPassword;
}