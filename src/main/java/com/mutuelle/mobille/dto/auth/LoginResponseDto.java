package com.mutuelle.mobille.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mutuelle.mobille.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // N'inclut que les champs non-null
public class LoginResponseDto {
    private Long authId;
    private Role role;
    private Long userRefId;
    private String accessToken;
    private String refreshToken;
    private String email;

    // Profils typés (un seul sera non-null)
    private Object profile;
}