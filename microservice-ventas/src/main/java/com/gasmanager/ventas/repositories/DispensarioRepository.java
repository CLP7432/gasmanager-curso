package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.Dispensario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispensarioRepository extends JpaRepository<Dispensario, Long> {

    List<Dispensario> findByActivoTrueOrderByIdAsc();
}