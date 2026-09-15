package com.gasmanager.ventas.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AceiteInventarioDTO {

    private Long id;
    private String nombre;
    private String tipoAceite;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private Boolean activo;
}
