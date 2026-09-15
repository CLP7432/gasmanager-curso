package com.gasmanager.ventas.services;

import com.gasmanager.ventas.dto.TurnoDTO;
import com.gasmanager.ventas.entities.Turno;
import com.gasmanager.ventas.enums.EstadoTurno;
import com.gasmanager.ventas.exceptions.RecursoNoEncontradoException;
import com.gasmanager.ventas.repositories.DispensarioRepository;
import com.gasmanager.ventas.repositories.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final DispensarioRepository dispensarioRepository;

    public TurnoDTO aDTO(Turno t) {
        return TurnoDTO.builder()
                .id(t.getId())
                .codigoTurno(t.getCodigoTurno())
                .nombre(t.getNombre())
                .fechaTurno(t.getFechaTurno() != null ? t.getFechaTurno().toString() : null)
                .horaInicio(t.getHoraInicio() != null ? t.getHoraInicio().toString() : null)
                .horaFin(t.getHoraFin() != null ? t.getHoraFin().toString() : null)
                .estado(t.getEstado() != null ? t.getEstado().name() : null)
                .supervisorId(t.getSupervisorId())
                .supervisorNombre(t.getSupervisorNombre())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TurnoDTO> listar(String estado) {
        if (estado != null && !estado.isBlank()) {
            return turnoRepository.findByEstadoOrderByFechaTurnoDesc(EstadoTurno.valueOf(estado.toUpperCase()))
                    .stream().map(this::aDTO).toList();
        }
        return turnoRepository.findAllByOrderByFechaTurnoDesc().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public TurnoDTO obtenerPorId(Long id) {
        return aDTO(turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado con id: " + id)));
    }

    @Transactional
    public TurnoDTO abrir(String nombre, String fechaTurno, Long supervisorId, String supervisorNombre) {
        if (turnoRepository.existsByEstado(EstadoTurno.ABIERTO)) {
            throw new IllegalArgumentException("Ya existe un turno ABIERTO. Ciérralo antes de abrir otro.");
        }
        LocalDate fecha = fechaTurno != null ? LocalDate.parse(fechaTurno) : LocalDate.now();
        Turno turno = Turno.builder()
                .codigoTurno(generarCodigo())
                .nombre(nombre != null && !nombre.isBlank() ? nombre : proximoNombreDisponible())
                .fechaTurno(fecha)
                .horaInicio(LocalDateTime.now())
                .estado(EstadoTurno.ABIERTO)
                .supervisorId(supervisorId)
                .supervisorNombre(supervisorNombre)
                .build();
        return aDTO(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoDTO cerrar(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado con id: " + id));
        if (turno.getEstado() == EstadoTurno.CERRADO) {
            throw new IllegalArgumentException("El turno " + turno.getCodigoTurno() + " ya está cerrado");
        }
        turno.setEstado(EstadoTurno.CERRADO);
        turno.setHoraFin(LocalDateTime.now());
        turnoRepository.save(turno);
        // El despachador solo está ligado a la isla durante el turno:
        // al cerrar se liberan todas las islas (quedan sin despachador
        // hasta que se abra el siguiente turno y se reasignen).
        dispensarioRepository.findAll().forEach(d -> {
            d.setDespachadorId(null);
            d.setDespachadorNombre(null);
        });
        return aDTO(turno);
    }

    private String generarCodigo() {
        Long numero = turnoRepository.findFirstByOrderByIdDesc()
                .map(t -> Long.parseLong(t.getCodigoTurno().substring(4)))
                .orElse(0L) + 1;
        return String.format("TUR-%05d", numero);
    }

    private String proximoNombreDisponible() {
        int numero = 1;
        String nombre;
        do {
            nombre = String.format("Turno %04d", numero);
            numero++;
        } while (turnoRepository.existsByNombre(nombre));
        return nombre;
    }
}