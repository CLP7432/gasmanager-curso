package com.gasmanager.lealtad.repositories;

import com.gasmanager.lealtad.entities.ProgramaLealtad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramaRepository extends JpaRepository<ProgramaLealtad, Long> {
    Optional<ProgramaLealtad> findFirstByActivoTrueOrderByIdDesc();
    List<ProgramaLealtad> findByActivoTrue();
}
