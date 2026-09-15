package com.gasmanager.facturacion.repositories;

import com.gasmanager.facturacion.entities.Factura;
import com.gasmanager.facturacion.enums.EstadoFactura;
import com.gasmanager.facturacion.enums.OrigenConceptoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findAllByOrderByFechaEmisionDesc();

    List<Factura> findByClienteFiscalIdOrderByFechaEmisionDesc(Long clienteFiscalId);

    Optional<Factura> findByUuid(String uuid);

    long countByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);

    @Query("select count(f) from Factura f join f.conceptos c " +
            "where c.origen = :origen and c.origenFolio = :origenFolio " +
            "and f.estado <> :estadoCancelada")
    long countConceptosYaFacturados(@Param("origen") OrigenConceptoFactura origen,
                                    @Param("origenFolio") String origenFolio,
                                    @Param("estadoCancelada") EstadoFactura estadoCancelada);

    List<Factura> findByEstadoOrderByFechaEmisionDesc(EstadoFactura estado);
}