package com.mutuelle.mobille.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String phone;
    private String avatar;
    private String pin;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}