package com.gasmanager.inventarios.repositories;

import com.gasmanager.inventarios.entities.Tanque;
import com.gasmanager.inventarios.enums.TipoCombustible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TanqueRepository extends JpaRepository<Tanque, Long> {
    List<Tanque> findAllByOrderByNombreAsc();
    List<Tanque> findByActivoTrue();
    Optional<Tanque> findByTipoCombustible(TipoCombustible tipo);
    Optional<Tanque> findFirstByTipoCombustibleAndActivoTrueOrderByIdAsc(TipoCombustible tipo);
    Optional<Tanque> findByNombre(String nombre);
}