package com.mutuelle.mobille.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilitaires pour la manipulation monétaire (arrondi, multiples, etc.)
 */
@UtilityClass
public class MoneyUtil {

    private static final BigDecimal MULTIPLE_25 = new BigDecimal("25");
    private static final int SCALE = 2;
    private static final RoundingMode ROUND_DOWN = RoundingMode.FLOOR;

    /**
     * Arrondit un montant à l'inférieur au multiple de 25 le plus proche.
     * Exemples :
     * 127 → 125
     * 150 → 150
     * 173 → 150
     */
    public static BigDecimal floorToNearest25(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Division entière : combien de fois 25 rentre dans le montant
        BigDecimal quotient = montant.divide(MULTIPLE_25, 0, RoundingMode.FLOOR);

        // Retour : quotient * 25
        return quotient.multiply(MULTIPLE_25).setScale(SCALE, ROUND_DOWN);
    }

    /**
     * Arrondi standard à 2 décimales (HALF_UP)
     */
    public static BigDecimal round(BigDecimal montant) {
        return montant != null ? montant.setScale(SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * Vérifie si un montant est un multiple exact de 25
     */
    public static boolean isMultipleOf25(BigDecimal montant) {
        return montant != null && montant.remainder(MULTIPLE_25).compareTo(BigDecimal.ZERO) == 0;
    }
}