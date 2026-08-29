package com.gasmanager.users.repositories;

import com.gasmanager.users.entities.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    Optional<Permiso> findByCodigoPermiso(String codigoPermiso);
    boolean existsByCodigoPermiso(String codigoPermiso);

}
