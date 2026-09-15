package com.gasmanager.clientes.repositories;

import com.gasmanager.clientes.entities.NotaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long> {

    List<NotaCredito> findByCreditoIdOrderByCreatedAtDesc(Long creditoId);

    Optional<NotaCredito> findByNumero(String numero);

    List<NotaCredito> findByCredito_Cliente_IdOrderByCreatedAtDesc(Long clienteId);

    List<NotaCredito> findByFechaCargaBetween(LocalDate desde, LocalDate hasta);
}