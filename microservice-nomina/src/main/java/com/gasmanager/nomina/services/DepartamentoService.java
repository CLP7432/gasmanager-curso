package com.gasmanager.nomina.services;

import com.gasmanager.nomina.dto.DepartamentoDTO;
import com.gasmanager.nomina.entities.Departamento;
import com.gasmanager.nomina.exceptions.RecursoNoEncontradoException;
import com.gasmanager.nomina.exceptions.ValidacionException;
import com.gasmanager.nomina.repositories.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    private DepartamentoDTO aDTO(Departamento departamento) {
        return DepartamentoDTO.builder()
                .id(departamento.getId())
                .nombre(departamento.getNombre())
                .descripcion(departamento.getDescripcion())
                .activo(departamento.getActivo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listar() {
        return departamentoRepository.findAll().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<DepartamentoDTO> listarActivos() {
        return departamentoRepository.findByActivoTrue().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public DepartamentoDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    private Departamento buscar(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Departamento no encontrado con id " + id));
    }

    @Transactional
    public DepartamentoDTO crear(DepartamentoDTO dto) {
        if (departamentoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidacionException("Ya existe un departamento con el nombre " + dto.getNombre());
        }
        Departamento departamento = Departamento.builder()
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();
        return aDTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public DepartamentoDTO actualizar(Long id, DepartamentoDTO dto) {
        Departamento departamento = buscar(id);
        if (dto.getNombre() != null) {
            if (departamentoRepository.existsByNombreIgnoreCase(dto.getNombre())
                    && !departamento.getNombre().equalsIgnoreCase(dto.getNombre())) {
                throw new ValidacionException("Ya existe un departamento con el nombre " + dto.getNombre());
            }
            departamento.setNombre(dto.getNombre().trim());
        }
        if (dto.getDescripcion() != null) departamento.setDescripcion(dto.getDescripcion());
        return aDTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public DepartamentoDTO toggle(Long id) {
        Departamento departamento = buscar(id);
        if (Boolean.TRUE.equals(departamento.getActivo())
                && departamentoRepository.countByEmpleados_Id(id) > 0) {
            throw new ValidacionException("No se puede desactivar el departamento porque tiene empleados asignados");
        }
        departamento.setActivo(!Boolean.TRUE.equals(departamento.getActivo()));
        return aDTO(departamentoRepository.save(departamento));
    }

    @Transactional
    public void eliminar(Long id) {
        Departamento departamento = buscar(id);
        if (departamentoRepository.countByEmpleados_Id(id) > 0) {
            throw new ValidacionException("No se puede eliminar el departamento porque tiene empleados asignados");
        }
        departamentoRepository.delete(departamento);
    }
}