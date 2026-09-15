package com.gasmanager.nomina.services;

import com.gasmanager.nomina.dto.PuestoDTO;
import com.gasmanager.nomina.entities.Puesto;
import com.gasmanager.nomina.exceptions.RecursoNoEncontradoException;
import com.gasmanager.nomina.exceptions.ValidacionException;
import com.gasmanager.nomina.repositories.PuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PuestoService {

    private final PuestoRepository puestoRepository;

    private PuestoDTO aDTO(Puesto puesto) {
        return PuestoDTO.builder()
                .id(puesto.getId())
                .nombre(puesto.getNombre())
                .descripcion(puesto.getDescripcion())
                .salarioBase(puesto.getSalarioBase())
                .salarioDiario(puesto.getSalarioDiario())
                .riesgoPuesto(puesto.getRiesgoPuesto())
                .activo(puesto.getActivo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PuestoDTO> listar() {
        return puestoRepository.findAll().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PuestoDTO> listarActivos() {
        return puestoRepository.findByActivoTrue().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public PuestoDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    private Puesto buscar(Long id) {
        return puestoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Puesto no encontrado con id " + id));
    }

    @Transactional
    public PuestoDTO crear(PuestoDTO dto) {
        if (puestoRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ValidacionException("Ya existe un puesto con el nombre " + dto.getNombre());
        }
        Puesto puesto = Puesto.builder()
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .salarioBase(dto.getSalarioBase())
                .salarioDiario(derivarSalarioDiario(dto))
                .riesgoPuesto(dto.getRiesgoPuesto())
                .activo(true)
                .build();
        return aDTO(puestoRepository.save(puesto));
    }

    private BigDecimal derivarSalarioDiario(PuestoDTO dto) {
        if (dto.getSalarioDiario() != null) return dto.getSalarioDiario();
        if (dto.getSalarioBase() != null) {
            return dto.getSalarioBase().divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        }
        return null;
    }

    @Transactional
    public PuestoDTO actualizar(Long id, PuestoDTO dto) {
        Puesto puesto = buscar(id);
        if (dto.getNombre() != null) {
            if (puestoRepository.existsByNombreIgnoreCase(dto.getNombre())
                    && !puesto.getNombre().equalsIgnoreCase(dto.getNombre())) {
                throw new ValidacionException("Ya existe un puesto con el nombre " + dto.getNombre());
            }
            puesto.setNombre(dto.getNombre().trim());
        }
        if (dto.getDescripcion() != null) puesto.setDescripcion(dto.getDescripcion());
        if (dto.getSalarioBase() != null) puesto.setSalarioBase(dto.getSalarioBase());
        if (dto.getSalarioDiario() != null) puesto.setSalarioDiario(dto.getSalarioDiario());
        if (dto.getRiesgoPuesto() != null) puesto.setRiesgoPuesto(dto.getRiesgoPuesto());
        return aDTO(puestoRepository.save(puesto));
    }

    @Transactional
    public PuestoDTO toggle(Long id) {
        Puesto puesto = buscar(id);
        if (Boolean.TRUE.equals(puesto.getActivo())
                && puestoRepository.countByEmpleados_Id(id) > 0) {
            throw new ValidacionException("No se puede desactivar el puesto porque tiene empleados asignados");
        }
        puesto.setActivo(!Boolean.TRUE.equals(puesto.getActivo()));
        return aDTO(puestoRepository.save(puesto));
    }

    @Transactional
    public void eliminar(Long id) {
        Puesto puesto = buscar(id);
        if (puestoRepository.countByEmpleados_Id(id) > 0) {
            throw new ValidacionException("No se puede eliminar el puesto porque tiene empleados asignados");
        }
        puestoRepository.delete(puesto);
    }
}