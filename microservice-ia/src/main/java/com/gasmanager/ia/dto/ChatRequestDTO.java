package com.gasmanager.ia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequestDTO {
    @NotBlank(message = "El mensaje es obligatorio")
    private String mensaje;

    // VENTAS, CLIENTES, INVENTARIOS, NOMINA, FACTURACION, ADMIN, GENERAL
    private String contexto;
    private String usuarioId;
    private String usuarioNombre;
}
