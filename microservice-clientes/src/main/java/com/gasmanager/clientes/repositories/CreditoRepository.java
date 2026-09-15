package com.gasmanager.clientes.repositories;

import com.gasmanager.clientes.entities.Credito;
import com.gasmanager.clientes.enums.EstadoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditoRepository extends JpaRepository<Credito, Long> {

    Optional<Credito> findByFolioCredito(String folioCredito);

    List<Credito> findByClienteId(Long clienteId);

    List<Credito> findByEstado(EstadoCredito estado);

    List<Credito> findByFechaVencimientoBeforeAndEstado(LocalDate fecha, EstadoCredito estado);

    @Query("select c from Credito c where c.saldoPendiente > 0 and c.estado = 'ACTIVO' and (c.fechaVencimiento is null or c.fechaVencimiento >= CURRENT_DATE) order by c.cliente.razonSocial")
    List<Credito> findCreditosActivosConSaldo();
}
