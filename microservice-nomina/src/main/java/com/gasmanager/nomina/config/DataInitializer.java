package com.gasmanager.nomina.config;

import com.gasmanager.nomina.entities.Departamento;
import com.gasmanager.nomina.entities.Puesto;
import com.gasmanager.nomina.enums.RiesgoPuesto;
import com.gasmanager.nomina.repositories.DepartamentoRepository;
import com.gasmanager.nomina.repositories.PuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PuestoRepository puestoRepository;
    private final DepartamentoRepository departamentoRepository;

    @Value("${app.initial-data.enabled:true}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (!enabled) return;
        crearPuestos();
        crearDepartamentos();
    }

    private void crearPuestos() {
        crearPuestoSiNoExiste("Despachador", "Atiende el despacho de combustible en el punto de venta",
                new BigDecimal("7800.00"), RiesgoPuesto.MEDIO);
        crearPuestoSiNoExiste("Limpieza", "Personal de limpieza e higiene de la estación",
                new BigDecimal("6200.00"), RiesgoPuesto.BAJO);
        crearPuestoSiNoExiste("Secretaria", "Apoyo administrativo y recepción",
                new BigDecimal("7000.00"), RiesgoPuesto.BAJO);
        crearPuestoSiNoExiste("Gerente", "Encargado general de la estación",
                new BigDecimal("15000.00"), RiesgoPuesto.MEDIO);
        crearPuestoSiNoExiste("Encargado", "Supervisa turnos y operación diaria",
                new BigDecimal("9000.00"), RiesgoPuesto.MEDIO);
    }

    private void crearPuestoSiNoExiste(String nombre, String descripcion, BigDecimal salarioBase, RiesgoPuesto riesgo) {
        if (puestoRepository.existsByNombreIgnoreCase(nombre)) return;
        Puesto puesto = Puesto.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .salarioBase(salarioBase)
                .salarioDiario(salarioBase.divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP))
                .riesgoPuesto(riesgo)
                .activo(true)
                .build();
        puestoRepository.save(puesto);
    }

    private void crearDepartamentos() {
        crearDepartamentoSiNoExiste("Operaciones", "Despacho de combustible y atención al cliente");
        crearDepartamentoSiNoExiste("Administración", "Control administrativo, caja y compras");
        crearDepartamentoSiNoExiste("Mantenimiento", "Mantenimiento de instalaciones y bombas");
    }

    private void crearDepartamentoSiNoExiste(String nombre, String descripcion) {
        if (departamentoRepository.existsByNombreIgnoreCase(nombre)) return;
        departamentoRepository.save(Departamento.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .activo(true)
                .build());
    }
}