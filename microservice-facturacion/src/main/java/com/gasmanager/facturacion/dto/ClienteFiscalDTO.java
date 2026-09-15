package com.gasmanager.facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteFiscalDTO {

    private Long id;

    private Long clienteId;

    @NotBlank(message = "El RFC es obligatorio")
    private String rfc;

    private String razonSocial;

    @NotBlank(message = "El régimen fiscal es obligatorio")
    private String regimenFiscal;

    @NotBlank(message = "El código postal es obligatorio")
    private String codigoPostal;

    private String usoCfdi;

    private String correo;
}