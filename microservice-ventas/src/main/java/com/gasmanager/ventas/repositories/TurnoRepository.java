package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.Turno;
import com.gasmanager.ventas.enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    Optional<Turno> findFirstByOrderByIdDesc();
    List<Turno> findAllByOrderByFechaTurnoDesc();
    List<Turno> findByEstadoOrderByFechaTurnoDesc(EstadoTurno estado);
    boolean existsByEstado(EstadoTurno estado);
    boolean existsByNombre(String nombre);
}