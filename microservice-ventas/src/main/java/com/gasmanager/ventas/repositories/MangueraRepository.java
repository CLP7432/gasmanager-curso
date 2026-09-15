package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.Manguera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MangueraRepository extends JpaRepository<Manguera, Long> {

    List<Manguera> findByActivoTrue();
}