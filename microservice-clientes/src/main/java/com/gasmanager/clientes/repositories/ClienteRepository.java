package com.gasmanager.clientes.repositories;

import com.gasmanager.clientes.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCodigoCliente(String codigoCliente);
    Optional<Cliente> findByRfc(String rfc);
    List<Cliente> findByActivoTrue();
    boolean existsByRfc(String rfc);
    List<Cliente> findByRazonSocialContainingIgnoreCase(String razonSocial);
}
