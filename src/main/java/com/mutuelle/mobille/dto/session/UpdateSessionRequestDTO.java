package com.mutuelle.mobille.dto.session;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateSessionRequestDTO {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String name;

    /*@NotNull(message = "Le montant de solidarité est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal solidarityAmount;*/

    @NotNull(message = "Le montant de l'agape est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal agapeAmountPerMember;

}
