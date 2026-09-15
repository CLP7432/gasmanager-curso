package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaIdOrderByIdAsc(Long ventaId);
}