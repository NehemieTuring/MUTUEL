package com.mutuelle.mobille.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PinUpdateDTO {
    @NotBlank
    @Size(min = 4, max = 4)
    private String oldPin;

    @NotBlank @Size(min = 4, max = 4)
    @Pattern(regexp = "^\\d{4}$", message = "Le PIN doit être composé de 4 chiffres")
    private String newPin;
}