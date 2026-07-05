package com.mutuelle.mobille.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailUpdateDTO {
    @NotBlank
    @Email
    private String newEmail;

    @NotBlank
    private String password;
}