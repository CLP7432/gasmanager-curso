package com.gasmanager.facturacion.dto;

import com.gasmanager.facturacion.enums.EstadoFactura;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FacturaResponseDTO {

    private Long id;
    private String folio;
    private String uuid;
    private String serie;
    private LocalDateTime fechaEmision;
    private Long clienteFiscalId;
    private Long clienteId;
    private String receptorRfc;
    private String receptorNombre;
    private String receptorRegimenFiscal;
    private String receptorCodigoPostal;
    private String receptorUsoCfdi;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal ieps;
    private BigDecimal total;
    private String formaPago;
    private String metodoPago;
    private EstadoFactura estado;
    private boolean timbrada;
    private LocalDateTime fechaTimbrado;
    private boolean correoEnviado;
    private List<FacturaConceptoDTO> conceptos;
}