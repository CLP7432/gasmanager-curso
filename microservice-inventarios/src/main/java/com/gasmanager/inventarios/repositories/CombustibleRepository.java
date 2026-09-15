package com.gasmanager.inventarios.repositories;

import com.gasmanager.inventarios.entities.Combustible;
import com.gasmanager.inventarios.enums.TipoCombustible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CombustibleRepository extends JpaRepository<Combustible, Long> {

    Optional<Combustible> findByTipo(TipoCombustible tipoCombustible);
    List<Combustible> findByActivoTrue();
    boolean existsByTipo(TipoCombustible tipoCombustible);
}
