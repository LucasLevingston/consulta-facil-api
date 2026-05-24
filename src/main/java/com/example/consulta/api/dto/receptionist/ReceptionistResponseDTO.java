package com.example.consulta.api.dto.receptionist;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReceptionistResponseDTO {
    private String id;
    private String userId;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
