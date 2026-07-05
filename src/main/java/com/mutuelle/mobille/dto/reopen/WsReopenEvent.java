package com.mutuelle.mobille.dto.reopen;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WsReopenEvent {

    private String type;
    private Long requestId;
    private Long sessionId;
    private String sessionName;
    private String message;
    private Boolean presidentApproved;
    private Boolean tresorierApproved;

    public static final String REOPEN_REQUESTED  = "REOPEN_REQUESTED";
    public static final String APPROVAL_UPDATE   = "APPROVAL_UPDATE";
    public static final String REOPEN_COMPLETED  = "REOPEN_COMPLETED";
    public static final String REOPEN_REJECTED   = "REOPEN_REJECTED";
    public static final String REOPEN_CANCELLED  = "REOPEN_CANCELLED";
}
