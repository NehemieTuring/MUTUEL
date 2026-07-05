package com.mutuelle.mobille.dto.reopen;

import com.mutuelle.mobille.enums.StatusReopenRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionReopenRequestResponseDTO {

    private Long id;
    private Long sessionId;
    private String sessionName;
    private String requestedByEmail;
    private LocalDateTime requestedAt;
    private StatusReopenRequest status;
    private Boolean presidentApproved;
    private LocalDateTime presidentApprovedAt;
    private Boolean tresorierApproved;
    private LocalDateTime tresorierApprovedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
