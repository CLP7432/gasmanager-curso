package com.gasmanager.ventas.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaraDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private Boolean activo;

    @Builder.Default
    private List<MangueraDTO> mangueras = new ArrayList<>();
}