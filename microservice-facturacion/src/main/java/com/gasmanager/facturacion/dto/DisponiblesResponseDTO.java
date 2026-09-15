package com.gasmanager.facturacion.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DisponiblesResponseDTO {

    private Long clienteId;
    private String clienteNombre;
    private List<ItemDisponibleDTO> notas;
}