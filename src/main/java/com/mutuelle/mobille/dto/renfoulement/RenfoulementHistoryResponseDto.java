package com.mutuelle.mobille.dto.renfoulement;

import com.mutuelle.mobille.dto.account.AccountMemberDTO;
import com.mutuelle.mobille.dto.account.AccountMemberFullDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RenfoulementHistoryResponseDto {
    private List<RenfoulementHistoryItemDto> pastRenfoulements;
    private RenfoulementSimulationDto currentSimulation;
    private List<AccountMemberFullDTO> accountMembers;
}