package com.mutuelle.mobille.dto.epargne;

import com.mutuelle.mobille.enums.TransactionDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Données nécessaires pour créer une transaction d'épargne")
public class EpargneRequestDto {

    @NotNull(message = "L'ID du membre est obligatoire")
    @Min(value = 1, message = "L'ID du membre doit être positif")
    @Schema(description = "Identifiant du membre concerné", example = "1")
    private Long memberId;

//    @NotNull(message = "L'ID de la session est obligatoire")
//    @Min(value = 1, message = "L'ID de la session doit être positif")
//    @Schema(description = "Identifiant de la session", example = "3")
//    private Long sessionId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être strictement positif")
    @Schema(description = "Montant de l'épargne", example = "5000.00")
    private BigDecimal amount;

    @NotNull(message = "La direction de la transaction est obligatoire")
    @Schema(description = "Direction de la transaction (CREDIT ou DEBIT)", example = "CREDIT",
            allowableValues = {"CREDIT", "DEBIT"})
    private TransactionDirection transactionDirection;

}