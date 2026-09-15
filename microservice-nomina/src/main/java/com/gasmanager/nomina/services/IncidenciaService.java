package com.gasmanager.nomina.services;

import com.gasmanager.nomina.dto.IncidenciaDTO;
import com.gasmanager.nomina.entities.Empleado;
import com.gasmanager.nomina.entities.Incidencia;
import com.gasmanager.nomina.exceptions.RecursoNoEncontradoException;
import com.gasmanager.nomina.repositories.EmpleadoRepository;
import com.gasmanager.nomina.repositories.IncidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final EmpleadoRepository empleadoRepository;

    private IncidenciaDTO aDTO(Incidencia incidencia) {
        return IncidenciaDTO.builder()
                .id(incidencia.getId())
                .empleadoId(incidencia.getEmpleado().getId())
                .empleadoNombre(incidencia.getEmpleado().getNombreCompleto())
                .tipo(incidencia.getTipo())
                .fecha(incidencia.getFecha())
                .cantidad(incidencia.getCantidad())
                .monto(incidencia.getMonto())
                .observaciones(incidencia.getObservaciones())
                .autorizadoPor(incidencia.getAutorizadoPor())
                .build();
    }

    private Incidencia buscar(Long id) {
        return incidenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Incidencia no encontrada con id " + id));
    }

    private Empleado buscarEmpleado(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<IncidenciaDTO> listar() {
        return incidenciaRepository.listarTodas().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidenciaDTO> listarPorEmpleado(Long empleadoId) {
        return incidenciaRepository.listarPorEmpleado(empleadoId).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidenciaDTO> listarPorEmpleadoYPeriodo(Long empleadoId, LocalDate inicio, LocalDate fin) {
        return incidenciaRepository.listarPorEmpleadoYPeriodo(empleadoId, inicio, fin).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public IncidenciaDTO obtenerPorId(Long id) {
        return aDTO(buscar(id));
    }

    @Transactional
    public IncidenciaDTO crear(IncidenciaDTO dto) {
        Incidencia incidencia = Incidencia.builder()
                .empleado(buscarEmpleado(dto.getEmpleadoId()))
                .tipo(dto.getTipo())
                .fecha(dto.getFecha())
                .cantidad(dto.getCantidad())
                .monto(dto.getMonto())
                .observaciones(dto.getObservaciones())
                .autorizadoPor(dto.getAutorizadoPor())
                .build();
        return aDTO(incidenciaRepository.save(incidencia));
    }

    @Transactional
    public IncidenciaDTO actualizar(Long id, IncidenciaDTO dto) {
        Incidencia incidencia = buscar(id);
        if (dto.getEmpleadoId() != null) incidencia.setEmpleado(buscarEmpleado(dto.getEmpleadoId()));
        if (dto.getTipo() != null) incidencia.setTipo(dto.getTipo());
        if (dto.getFecha() != null) incidencia.setFecha(dto.getFecha());
        if (dto.getCantidad() != null) incidencia.setCantidad(dto.getCantidad());
        if (dto.getMonto() != null) incidencia.setMonto(dto.getMonto());
        if (dto.getObservaciones() != null) incidencia.setObservaciones(dto.getObservaciones());
        if (dto.getAutorizadoPor() != null) incidencia.setAutorizadoPor(dto.getAutorizadoPor());
        return aDTO(incidenciaRepository.save(incidencia));
    }

    @Transactional
    public void eliminar(Long id) {
        incidenciaRepository.delete(buscar(id));
    }
}