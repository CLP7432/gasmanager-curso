package com.gasmanager.inventarios.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PendienteCargaDTO {

    private Long compraId;
    private String folioFactura;
    private String fechaFactura;
    private String tipoCompra;

    private Long detalleId;
    private Long combustibleId;
    private String combustibleNombre;
    private String tipoCombustible;

    private BigDecimal litrosComprados;
    private BigDecimal litrosDescargados;
    private BigDecimal litrosPendientes;

    private Long tanqueId;
    private String tanqueNombre;
    private BigDecimal tanqueCapacidad;
    private BigDecimal tanqueEspacioDisponible;
}