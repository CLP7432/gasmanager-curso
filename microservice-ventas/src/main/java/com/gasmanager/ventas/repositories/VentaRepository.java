package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.Venta;
import com.gasmanager.ventas.enums.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    Optional<Venta> findByFolio(String folio);
    Optional<Venta> findFirstByOrderByIdDesc();
    List<Venta> findAllByOrderByFechaHoraDesc();
    List<Venta> findByEstadoOrderByFechaHoraDesc(EstadoVenta estado);
    List<Venta> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByTurnoIdOrderByFechaHoraAsc(Long turnoId);
    List<Venta> findByTurnoIdAndDispensarioIdOrderByFechaHoraAsc(Long turnoId, Long dispensarioId);
}