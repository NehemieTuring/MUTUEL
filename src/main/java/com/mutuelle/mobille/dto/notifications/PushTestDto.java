package com.mutuelle.mobille.dto.notifications;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushTestDto {

    @NotBlank(message = "Le titre est requis")
    private String title;

    @NotBlank(message = "Le message est requis")
    private String message;

    @NotNull(message = "L'id user est requis")
    @Min(value = 1, message = "L'id user doit être positif")
    private Long userId;
}