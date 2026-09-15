package com.gasmanager.lealtad.repositories;

import com.gasmanager.lealtad.entities.TransaccionPuntos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<TransaccionPuntos, Long> {
    List<TransaccionPuntos> findByVentaIdOrderByFechaDesc(Long ventaId);
}
