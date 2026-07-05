package com.mutuelle.mobille.service;

import com.mutuelle.mobille.dto.sessionHistory.SessionHistoryResponseDTO;
import com.mutuelle.mobille.models.SessionHistory;
import com.mutuelle.mobille.repository.ExerciceRepository;
import com.mutuelle.mobille.repository.SessionHistoryRepository;
import com.mutuelle.mobille.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionHistoryService {

    private final SessionHistoryRepository sessionHistoryRepository;
    private final SessionRepository sessionRepository;
    private final ExerciceRepository exerciceRepository;
    private final BilanService bilanService;

    public List<SessionHistoryResponseDTO> getAllHistory() {
        return sessionHistoryRepository.findAll().stream()
                .map(bilanService::toDTO)
                .collect(Collectors.toList());
    }

    public List<SessionHistoryResponseDTO> getHistoryByExerciceId(Long exerciceId) {
        return sessionHistoryRepository.findAllByExerciceId(exerciceId).stream()
                .map(bilanService::toDTO)
                .collect(Collectors.toList());
    }

    public List<SessionHistoryResponseDTO> getHistoryBySessionId(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new RuntimeException("Session non trouvée avec l'id : " + sessionId);
        }

        Optional<SessionHistory> history = sessionHistoryRepository.findBySessionId(sessionId);

        return history.stream()
                .map(bilanService::toDTO)
                .collect(Collectors.toList());
    }
}
