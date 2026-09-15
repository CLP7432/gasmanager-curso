package com.gasmanager.ia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private String respuesta;
    private String modelo;
    private LocalDateTime timestamp;
    private boolean exito;
    private String error;
}
