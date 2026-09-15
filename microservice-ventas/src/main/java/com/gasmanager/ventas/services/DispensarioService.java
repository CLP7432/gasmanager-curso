package com.gasmanager.ventas.services;

import com.gasmanager.ventas.dto.CaraDTO;
import com.gasmanager.ventas.dto.DispensarioDTO;
import com.gasmanager.ventas.dto.MangueraDTO;
import com.gasmanager.ventas.entities.CaraDispensario;
import com.gasmanager.ventas.entities.Dispensario;
import com.gasmanager.ventas.entities.Manguera;
import com.gasmanager.ventas.exceptions.RecursoNoEncontradoException;
import com.gasmanager.ventas.repositories.DispensarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DispensarioService {

    private final DispensarioRepository dispensarioRepository;

    public DispensarioDTO aDTO(Dispensario d) {
        return DispensarioDTO.builder()
                .id(d.getId())
                .numero(d.getNumero())
                .nombre(d.getNombre())
                .ubicacion(d.getUbicacion())
                .activo(d.getActivo())
                .despachadorId(d.getDespachadorId())
                .despachadorNombre(d.getDespachadorNombre())
                .caras(d.getCaras().stream().map(c -> CaraDTO.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .activo(c.getActivo())
                        .mangueras(c.getMangueras().stream().map(m -> MangueraDTO.builder()
                                .id(m.getId())
                                .codigo(m.getCodigo())
                                .nombre(m.getNombre())
                                .tipoCombustible(m.getTipoCombustible())
                                .combustibleId(m.getCombustibleId())
                                .lecturaActual(m.getLecturaActual())
                                .activo(m.getActivo())
                                .build()).toList())
                        .build()).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DispensarioDTO> listarCompletos() {
        return dispensarioRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public DispensarioDTO obtenerCompleto(Long id) {
        return aDTO(dispensarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + id)));
    }

    @Transactional
    public DispensarioDTO crearCompleto(DispensarioDTO dto) {
        Dispensario dispensario = Dispensario.builder()
                .numero(dto.getNumero())
                .nombre(dto.getNombre())
                .ubicacion(dto.getUbicacion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
        for (CaraDTO caraDTO : dto.getCaras()) {
            CaraDispensario cara = CaraDispensario.builder()
                    .codigo(caraDTO.getCodigo())
                    .nombre(caraDTO.getNombre())
                    .activo(caraDTO.getActivo() != null ? caraDTO.getActivo() : true)
                    .dispensario(dispensario)
                    .build();
            for (MangueraDTO mDTO : caraDTO.getMangueras()) {
                cara.getMangueras().add(Manguera.builder()
                        .cara(cara)
                        .codigo(mDTO.getCodigo())
                        .nombre(mDTO.getNombre())
                        .tipoCombustible(mDTO.getTipoCombustible())
                        .combustibleId(mDTO.getCombustibleId())
                        .lecturaActual(mDTO.getLecturaActual() != null ? mDTO.getLecturaActual() : BigDecimal.ZERO)
                        .activo(mDTO.getActivo() != null ? mDTO.getActivo() : true)
                        .build());
            }
            dispensario.getCaras().add(cara);
        }
        return aDTO(dispensarioRepository.save(dispensario));
    }

    @Transactional
    public DispensarioDTO actualizarCompleto(Long id, DispensarioDTO dto) {
        Dispensario dispensario = dispensarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + id));

        Map<String, BigDecimal> lecturasExistentes = new HashMap<>();
        for (CaraDispensario cara : dispensario.getCaras()) {
            for (Manguera m : cara.getMangueras()) {
                lecturasExistentes.put(cara.getCodigo() + ":" + m.getCodigo(), m.getLecturaActual());
            }
        }

        dispensario.setNumero(dto.getNumero());
        dispensario.setNombre(dto.getNombre());
        dispensario.setUbicacion(dto.getUbicacion());
        if (dto.getActivo() != null) dispensario.setActivo(dto.getActivo());

        dispensario.getCaras().clear();
        for (CaraDTO caraDTO : dto.getCaras()) {
            CaraDispensario cara = CaraDispensario.builder()
                    .codigo(caraDTO.getCodigo())
                    .nombre(caraDTO.getNombre())
                    .activo(caraDTO.getActivo() != null ? caraDTO.getActivo() : true)
                    .dispensario(dispensario)
                    .build();
            for (MangueraDTO mDTO : caraDTO.getMangueras()) {
                BigDecimal lectura = lecturasExistentes.get(caraDTO.getCodigo() + ":" + mDTO.getCodigo());
                cara.getMangueras().add(Manguera.builder()
                        .cara(cara)
                        .codigo(mDTO.getCodigo())
                        .nombre(mDTO.getNombre())
                        .tipoCombustible(mDTO.getTipoCombustible())
                        .combustibleId(mDTO.getCombustibleId())
                        .lecturaActual(lectura != null
                                ? lectura
                                : (mDTO.getLecturaActual() != null ? mDTO.getLecturaActual() : BigDecimal.ZERO))
                        .activo(mDTO.getActivo() != null ? mDTO.getActivo() : true)
                        .build());
            }
            dispensario.getCaras().add(cara);
        }
        return aDTO(dispensarioRepository.save(dispensario));
    }

    @Transactional
    public DispensarioDTO asignarDespachador(Long id, Long despachadorId, String despachadorNombre) {
        Dispensario dispensario = dispensarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + id));
        dispensario.setDespachadorId(despachadorId);
        dispensario.setDespachadorNombre(despachadorNombre);
        return aDTO(dispensarioRepository.save(dispensario));
    }

    @Transactional
    public DispensarioDTO cambiarActivo(Long id, Boolean activo) {
        Dispensario dispensario = dispensarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + id));
        dispensario.setActivo(activo != null ? activo : !dispensario.getActivo());
        return aDTO(dispensarioRepository.save(dispensario));
    }
}