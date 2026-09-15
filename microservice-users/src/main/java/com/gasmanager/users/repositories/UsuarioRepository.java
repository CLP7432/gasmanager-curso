package com.gasmanager.users.repositories;

import com.gasmanager.users.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Usuario> findAllByActivoTrue();
    List<Usuario> findByBloqueadoTrue();
    long countByRolId(long rolId);


}
