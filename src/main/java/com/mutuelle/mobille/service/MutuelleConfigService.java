package com.mutuelle.mobille.service;

import com.mutuelle.mobille.models.MutuelleConfig;
import com.mutuelle.mobille.repository.MutuelleConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MutuelleConfigService {

    private final MutuelleConfigRepository configRepository;
    private  MemberService memberService;



    @Autowired
    @Lazy
    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostConstruct
    @Transactional
    public void initDefaultConfig() {
        if (configRepository.count() == 0) {
            MutuelleConfig defaultConfig = MutuelleConfig.builder()
                    .registrationFeeAmount(new BigDecimal("15000.00"))
                    .solidarityFeeAmount(new BigDecimal("150000.00"))
                    .loanInterestRatePercent(new BigDecimal("3.00"))
                    .loanPenaltySessionThreshold(3)
                    .loanPenaltyFixedAmount(new BigDecimal("15000.00"))
                    .insolvencyThreshold(new BigDecimal("250000.00"))
                    .updatedBy("system")
                    .build();
            configRepository.save(defaultConfig);
        }
    }

    @Cacheable(value = "mutuelleConfig", key = "'current'")
    public MutuelleConfig getCurrentConfig() {
        return configRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new RuntimeException("Configuration de la mutuelle non trouvée"));
    }

    @Transactional
    @CacheEvict(value = "mutuelleConfig", allEntries = true)
    public MutuelleConfig updateConfig(MutuelleConfig updatedConfig, String updatedBy) {
        MutuelleConfig config = getCurrentConfig();
        BigDecimal oldThreshold = config.getInsolvencyThreshold();
        boolean thresholdChanged = false;

        if (updatedConfig.getRegistrationFeeAmount() != null)
            config.setRegistrationFeeAmount(updatedConfig.getRegistrationFeeAmount());
        if (updatedConfig.getSolidarityFeeAmount() != null)
            config.setSolidarityFeeAmount(updatedConfig.getSolidarityFeeAmount());
        if (updatedConfig.getLoanInterestRatePercent() != null)
            config.setLoanInterestRatePercent(updatedConfig.getLoanInterestRatePercent());
        if (updatedConfig.getLoanPenaltyFixedAmount() != null)
            config.setLoanPenaltyFixedAmount(updatedConfig.getLoanPenaltyFixedAmount());
        if (updatedConfig.getLoanPenaltySessionThreshold() != null)
            config.setLoanPenaltySessionThreshold(updatedConfig.getLoanPenaltySessionThreshold());
        if (updatedConfig.getInsolvencyThreshold() != null) {
            config.setInsolvencyThreshold(updatedConfig.getInsolvencyThreshold());
            thresholdChanged = oldThreshold.compareTo(config.getInsolvencyThreshold()) != 0;
        }
        config.setUpdatedBy(updatedBy);
        MutuelleConfig saved = configRepository.save(config);
        if (thresholdChanged) {
            memberService.recalculateAllMemberStatuses();
        }

        return saved;
    }

}