package com.gasmanager.users.repositories;

import com.gasmanager.users.entities.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<SesionUsuario, Long> {

    Optional<SesionUsuario> findByToken(String token);
    List<SesionUsuario> findByIdUsuario(Long idUsuario);
    List<SesionUsuario> findByActivoTrue();
}
