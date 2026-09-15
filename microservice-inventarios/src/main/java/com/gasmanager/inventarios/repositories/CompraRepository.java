package com.gasmanager.inventarios.repositories;

import com.gasmanager.inventarios.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    Optional<Compra> findByFolioFactura(String folioFactura);
    List<Compra> findByProveedorIdOrderByFechaRegistroDesc(Long proveedorId);
    List<Compra> findAllByOrderByFechaRegistroDesc();
    List<Compra> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);
}