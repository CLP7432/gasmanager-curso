package com.gasmanager.facturacion.repositories;

import com.gasmanager.facturacion.entities.ClienteFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteFiscalRepository extends JpaRepository<ClienteFiscal, Long> {

    Optional<ClienteFiscal> findByClienteId(Long clienteId);

    boolean existsByClienteId(Long clienteId);

    Optional<ClienteFiscal> findByRfcIgnoreCase(String rfc);
}