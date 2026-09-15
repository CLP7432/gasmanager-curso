package com.gasmanager.nomina.repositories;

import com.gasmanager.nomina.entities.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

    List<Departamento> findByActivoTrue();

    boolean existsByNombreIgnoreCase(String nombre);

    long countByEmpleados_Id(Long empleadoId);
}