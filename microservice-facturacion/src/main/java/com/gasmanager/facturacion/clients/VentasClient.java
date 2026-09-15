package com.gasmanager.facturacion.clients;

import com.gasmanager.facturacion.dto.VentaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "microservice-ventas")
public interface VentasClient {

    @GetMapping("/api/ventas/folio/{folio}")
    VentaDTO obtenerVentaPorFolio(@PathVariable("folio") String folio);
}