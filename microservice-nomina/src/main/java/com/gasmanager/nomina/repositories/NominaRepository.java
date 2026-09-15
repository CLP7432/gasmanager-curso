package com.gasmanager.nomina.repositories;

import com.gasmanager.nomina.entities.Nomina;
import com.gasmanager.nomina.enums.EstadoNomina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface NominaRepository extends JpaRepository<Nomina, Long> {

    @Query("SELECT DISTINCT n FROM Nomina n LEFT JOIN FETCH n.detalles d LEFT JOIN FETCH d.empleado ORDER BY n.periodoInicio DESC, n.periodoFin DESC")
    List<Nomina> listarTodas();

    @Query("SELECT DISTINCT n FROM Nomina n LEFT JOIN FETCH n.detalles d LEFT JOIN FETCH d.empleado WHERE n.estado = :estado ORDER BY n.periodoInicio DESC, n.periodoFin DESC")
    List<Nomina> listarPorEstado(EstadoNomina estado);

    long countByPeriodoInicioLessThanEqualAndPeriodoFinGreaterThanEqual(LocalDate fechaFinParam, LocalDate fechaInicioParam);
}