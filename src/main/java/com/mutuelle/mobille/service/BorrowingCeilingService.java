package com.mutuelle.mobille.service;


import com.mutuelle.mobille.models.BorrowingCeilingInterval;
import com.mutuelle.mobille.repository.BorrowingCeilingIntervalRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingCeilingService {

    private final BorrowingCeilingIntervalRepository repository;

    /**
     * Charge automatiquement les valeurs par défaut si la table est vide.
     */
    @PostConstruct
    public void loadDefaultIntervals() {
        if (repository.count() == 0) {
            // Intervalle 1: 0 à 500_000, multiplicateur 5, plafond max 2_000_000
            repository.save(BorrowingCeilingInterval.builder()
                    .minEpargne(BigDecimal.ZERO)
                    .maxEpargne(BigDecimal.valueOf(500_000))
                    .multiplier(BigDecimal.valueOf(5))
                    .maxPlafond(BigDecimal.valueOf(2_000_000))
                    .build());

            // Intervalle 2: 500_001 à 1_000_000, multiplicateur 4, sans plafond max
            repository.save(BorrowingCeilingInterval.builder()
                    .minEpargne(BigDecimal.valueOf(500_000).add(BigDecimal.ONE))
                    .maxEpargne(BigDecimal.valueOf(1_000_000))
                    .multiplier(BigDecimal.valueOf(4))
                    .maxPlafond(null)
                    .build());

            // Intervalle 3: 1_000_001 à 1_500_000, multiplicateur 3, sans plafond max
            repository.save(BorrowingCeilingInterval.builder()
                    .minEpargne(BigDecimal.valueOf(1_000_000).add(BigDecimal.ONE))
                    .maxEpargne(BigDecimal.valueOf(1_500_000))
                    .multiplier(BigDecimal.valueOf(3))
                    .maxPlafond(null)
                    .build());

            // Intervalle 4: 1_500_001 à 2_000_000, multiplicateur 2, plafond max 4_000_000
            repository.save(BorrowingCeilingInterval.builder()
                    .minEpargne(BigDecimal.valueOf(1_500_000).add(BigDecimal.ONE))
                    .maxEpargne(BigDecimal.valueOf(2_000_000))
                    .multiplier(BigDecimal.valueOf(2))
                    .maxPlafond(BigDecimal.valueOf(4_000_000))
                    .build());

            // Intervalle 5: Au-dessus de 2_000_000, multiplicateur 1.5, sans plafond max
            repository.save(BorrowingCeilingInterval.builder()
                    .minEpargne(BigDecimal.valueOf(2_000_000).add(BigDecimal.ONE))
                    .maxEpargne(null) // Pas de limite supérieure
                    .multiplier(BigDecimal.valueOf(1.5))
                    .maxPlafond(null)
                    .build());
        }
    }

    /**
     * Calcule le plafond d'emprunt basé sur l'épargne en utilisant les intervalles stockés.
     * Les intervalles sont supposés non-chevauchants et triés par min_epargne.
     *
     * @param epargne L'épargne du membre.
     * @return Le plafond calculé.
     */
    public BigDecimal calculerPlafond(BigDecimal epargne) {
        List<BorrowingCeilingInterval> intervals = repository.findAllByOrderByMinEpargneAsc();

        for (BorrowingCeilingInterval interval : intervals) {
            if (epargne.compareTo(interval.getMinEpargne()) >= 0 &&
                    (interval.getMaxEpargne() == null || epargne.compareTo(interval.getMaxEpargne()) <= 0)) {
                BigDecimal plafond = epargne.multiply(interval.getMultiplier());
                if (interval.getMaxPlafond() != null) {
                    plafond = plafond.min(interval.getMaxPlafond());
                }
                return plafond;
            }
        }

        // Si aucun intervalle ne correspond (cas edge improbable si defaults chargés), retour 0
        return BigDecimal.ZERO;
    }

    public List<BorrowingCeilingInterval> getAllIntervalsOrdered() {
        return repository.findAllByOrderByMinEpargneAsc();
    }
}