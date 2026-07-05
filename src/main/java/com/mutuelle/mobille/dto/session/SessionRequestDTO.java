package com.mutuelle.mobille.dto.session;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SessionRequestDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String name;

    /*@NotNull(message = "Le montant de solidarité est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal solidarityAmount;*/

    @NotNull(message = "Le montant de l'agape est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal agapeAmountPerMember;

    {/*
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    private LocalDateTime endDate; // optionnelle
    */}

    @NotNull(message = "L'ID de l'exercice est obligatoire")
    private Long exerciceId;
}