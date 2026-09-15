package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.SurtidorAceite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurtidorAceiteRepository extends JpaRepository<SurtidorAceite, Long> {

    List<SurtidorAceite> findByActivoTrueOrderByIdAsc();
    Optional<SurtidorAceite> findByDespachadorIdAndActivoTrue(Long despachadorId);
    Optional<SurtidorAceite> findByDispensarioIdAndActivoTrue(Long dispensarioId);
}
