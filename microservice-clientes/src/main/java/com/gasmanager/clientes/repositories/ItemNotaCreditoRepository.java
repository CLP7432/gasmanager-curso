package com.gasmanager.clientes.repositories;

import com.gasmanager.clientes.entities.ItemNotaCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemNotaCreditoRepository extends JpaRepository<ItemNotaCredito, Long> {

    List<ItemNotaCredito> findByNotaCreditoId(Long notaCreditoId);
}