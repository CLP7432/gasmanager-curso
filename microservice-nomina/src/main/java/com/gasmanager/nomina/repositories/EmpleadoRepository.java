package com.gasmanager.nomina.repositories;

import com.gasmanager.nomina.entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.puesto LEFT JOIN FETCH e.departamento ORDER BY e.id")
    List<Empleado> listarTodos();

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.puesto LEFT JOIN FETCH e.departamento " +
            "WHERE e.activo = true AND (e.fechaBaja IS NULL OR e.fechaBaja > CURRENT_DATE) ORDER BY e.id")
    List<Empleado> listarActivos();

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.puesto LEFT JOIN FETCH e.departamento " +
            "WHERE e.puesto.id = :puestoId ORDER BY e.id")
    List<Empleado> listarPorPuesto(@Param("puestoId") Long puestoId);

    @Query("SELECT DISTINCT e FROM Empleado e LEFT JOIN FETCH e.puesto LEFT JOIN FETCH e.departamento " +
            "WHERE e.departamento.id = :departamentoId ORDER BY e.id")
    List<Empleado> listarPorDepartamento(@Param("departamentoId") Long departamentoId);

    Optional<Empleado> findByCodigoEmpleado(String codigoEmpleado);

    Optional<Empleado> findByRfc(String rfc);

    boolean existsByCodigoEmpleado(String codigoEmpleado);

    boolean existsByRfc(String rfc);

    long countByPuestoId(Long puestoId);

    long countByDepartamentoId(Long departamentoId);
    boolean existsByNombreAndApellidoPaternoAndApellidoMaterno(String nombre, String apellidoPaterno, String apellidoMaterno);
}