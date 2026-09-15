package com.gasmanager.users.repositories;

import com.gasmanager.users.entities.PasswordResetToken;
import com.gasmanager.users.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUsuario(Usuario usuario);
    void deleteByUsuario(Usuario usuario);
    boolean existsByTokenAndUsuarioFalse(String token);
}
