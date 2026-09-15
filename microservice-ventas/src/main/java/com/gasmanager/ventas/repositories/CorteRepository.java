package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.Corte;
import com.gasmanager.ventas.enums.EstadoCorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CorteRepository extends JpaRepository<Corte, Long> {

    Optional<Corte> findFirstByOrderByIdDesc();
    List<Corte> findAllByOrderByIdDesc();
    List<Corte> findByTurnoIdOrderByIdDesc(Long turnoId);
    Optional<Corte> findByTurnoIdAndDispensarioId(Long turnoId, Long dispensarioId);
    boolean existsByTurnoIdAndDispensarioId(Long turnoId, Long dispensarioId);
    List<Corte> findByEstadoOrderByIdDesc(EstadoCorte estado);
}