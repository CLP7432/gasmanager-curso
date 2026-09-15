package com.gasmanager.lealtad.services;

import com.gasmanager.lealtad.dto.CrearProgramaDTO;
import com.gasmanager.lealtad.dto.ProgramaDTO;
import com.gasmanager.lealtad.entities.ProgramaLealtad;
import com.gasmanager.lealtad.exceptions.RecursoNoEncontradoException;
import com.gasmanager.lealtad.repositories.ProgramaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgramaService {

    private final ProgramaRepository programaRepository;

    public ProgramaDTO aDTO(ProgramaLealtad p) {
        return ProgramaDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .puntosPorLitro(p.getPuntosPorLitro())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .activo(p.getActivo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProgramaDTO> listar() {
        return programaRepository.findAll().stream().map(this::aDTO).toList();
    }

    @Transactional
    public ProgramaDTO crear(CrearProgramaDTO dto) {
        ProgramaLealtad programa = ProgramaLealtad.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .puntosPorLitro(dto.getPuntosPorLitro())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .activo(false)
                .build();
        return aDTO(programaRepository.save(programa));
    }

    @Transactional
    public ProgramaDTO actualizar(Long id, CrearProgramaDTO dto) {
        ProgramaLealtad programa = buscar(id);
        if (dto.getNombre() != null && !dto.getNombre().isBlank()) programa.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) programa.setDescripcion(dto.getDescripcion());
        if (dto.getPuntosPorLitro() != null && dto.getPuntosPorLitro() > 0) programa.setPuntosPorLitro(dto.getPuntosPorLitro());
        if (dto.getFechaInicio() != null) programa.setFechaInicio(dto.getFechaInicio());
        if (dto.getFechaFin() != null) programa.setFechaFin(dto.getFechaFin());
        return aDTO(programaRepository.save(programa));
    }

    @Transactional
    public ProgramaDTO activar(Long id) {
        // Solo un programa activo a la vez: se apagan los demás
        programaRepository.findByActivoTrue().forEach(p -> p.setActivo(false));
        ProgramaLealtad programa = buscar(id);
        programa.setActivo(true);
        return aDTO(programaRepository.save(programa));
    }

    @Transactional
    public void desactivar(Long id) {
        ProgramaLealtad programa = buscar(id);
        programa.setActivo(false);
        programaRepository.save(programa);
    }

    @Transactional(readOnly = true)
    public Optional<ProgramaDTO> programaVigente() {
        return programaRepository.findFirstByActivoTrueOrderByIdDesc()
                .filter(p -> vigente(p))
                .map(this::aDTO);
    }

    private boolean vigente(ProgramaLealtad p) {
        if (p.getActivo() == null || !p.getActivo()) return false;
        LocalDate hoy = LocalDate.now();
        if (p.getFechaInicio() != null && hoy.isBefore(p.getFechaInicio())) return false;
        if (p.getFechaFin() != null && hoy.isAfter(p.getFechaFin())) return false;
        return true;
    }

    private ProgramaLealtad buscar(Long id) {
        return programaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Programa no encontrado con id: " + id));
    }
}
