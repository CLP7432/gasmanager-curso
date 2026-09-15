package com.gasmanager.inventarios.repositories;

import com.gasmanager.inventarios.entities.Proveedor;
import com.gasmanager.inventarios.enums.TipoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByActivoTrue();
    List<Proveedor> findByTipoProveedor(TipoProveedor tipoProveedor);
    Optional<Proveedor> findByRfc(String rfc);
    boolean existsByRfc(String rfc);
}