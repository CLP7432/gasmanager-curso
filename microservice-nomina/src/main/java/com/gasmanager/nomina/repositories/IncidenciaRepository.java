package com.gasmanager.nomina.repositories;

import com.gasmanager.nomina.entities.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    @Query("SELECT i FROM Incidencia i JOIN FETCH i.empleado ORDER BY i.fecha DESC, i.id DESC")
    List<Incidencia> listarTodas();

    @Query("SELECT i FROM Incidencia i JOIN FETCH i.empleado WHERE i.empleado.id = :empleadoId ORDER BY i.fecha DESC, i.id DESC")
    List<Incidencia> listarPorEmpleado(@Param("empleadoId") Long empleadoId);

    @Query("SELECT i FROM Incidencia i JOIN FETCH i.empleado " +
            "WHERE i.empleado.id = :empleadoId AND i.fecha BETWEEN :inicio AND :fin ORDER BY i.fecha, i.id")
    List<Incidencia> listarPorEmpleadoYPeriodo(@Param("empleadoId") Long empleadoId,
                                               @Param("inicio") LocalDate inicio,
                                               @Param("fin") LocalDate fin);
}