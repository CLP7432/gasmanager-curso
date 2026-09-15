package com.gasmanager.nomina.repositories;

import com.gasmanager.nomina.entities.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    List<Puesto> findByActivoTrue();

    boolean existsByNombreIgnoreCase(String nombre);

    long countByEmpleados_Id(Long empleadoId);
}