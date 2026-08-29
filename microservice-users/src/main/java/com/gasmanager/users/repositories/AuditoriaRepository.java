package com.gasmanager.users.repositories;

import com.gasmanager.users.entities.AuditoriaAccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaAccion, Long> {

    List<AuditoriaAccion> findByIdUsuarioEjecutor(Long idUsuario);
    List<AuditoriaAccion> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    List<AuditoriaAccion> findAllByOrderByFechaHoraDesc();
}
