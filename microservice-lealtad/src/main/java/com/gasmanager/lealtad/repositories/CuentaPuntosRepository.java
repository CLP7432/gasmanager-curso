package com.gasmanager.lealtad.repositories;

import com.gasmanager.lealtad.entities.CuentaPuntos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaPuntosRepository extends JpaRepository<CuentaPuntos, Long> {
    Optional<CuentaPuntos> findByVentaId(Long ventaId);
}
