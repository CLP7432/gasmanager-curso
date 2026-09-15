package com.gasmanager.inventarios.repositories;

import com.gasmanager.inventarios.entities.PrecioHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrecioHistoricoRepository extends JpaRepository<PrecioHistorico, Long> {
    List<PrecioHistorico> findByCombustibleIdOrderByFechaCambioDesc(Long combustibleId);
}
