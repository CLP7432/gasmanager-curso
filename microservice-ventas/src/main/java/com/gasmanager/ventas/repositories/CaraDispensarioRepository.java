package com.gasmanager.ventas.repositories;

import com.gasmanager.ventas.entities.CaraDispensario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaraDispensarioRepository extends JpaRepository<CaraDispensario, Long> {
}