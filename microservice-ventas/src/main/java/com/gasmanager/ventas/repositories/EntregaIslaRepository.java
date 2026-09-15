package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.EntregaIsla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EntregaIslaRepository extends JpaRepository<EntregaIsla, Long> {

    List<EntregaIsla> findBySurtidorAceiteIdOrderByFechaAsc(Long surtidorAceiteId);
    List<EntregaIsla> findBySurtidorAceiteIdAndTurnoIdOrderByFechaAsc(Long surtidorAceiteId, Long turnoId);
}
