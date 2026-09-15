package com.gasmanager.inventarios.repositories;

import com.gasmanager.inventarios.entities.Aceite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AceiteRepository extends JpaRepository<Aceite, Long>{

    Optional<Aceite> findByCodigo(String codigo);
    List<Aceite> findByActivoTrue();

}
