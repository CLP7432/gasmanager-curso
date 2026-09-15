package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.CorteAceite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorteAceiteRepository extends JpaRepository<CorteAceite, Long> {

    List<CorteAceite> findByCorteId(Long corteId);

    void deleteAllByCorteId(Long corteId);
}
