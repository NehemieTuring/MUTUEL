package com.mutuelle.mobille.dto.notifications;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushTokenDto {

    @NotBlank(message = "Le token est requis")
    private String token;
}