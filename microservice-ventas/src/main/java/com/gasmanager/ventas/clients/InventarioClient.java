package com.gasmanager.ventas.clients;

import com.gasmanager.ventas.dto.AceiteInventarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "microservice-inventarios")
public interface InventarioClient {

    @GetMapping("/api/aceites/{id}")
    AceiteInventarioDTO obtenerAceite(@PathVariable("id") Long id);

    @PostMapping("/api/aceites/{id}/disminuir-stock")
    void disminuirStock(
            @PathVariable("id") Long id,
            @RequestParam("cantidad") Integer cantidad,
            @RequestHeader("X-User-Id") Long idUsuario
    );
    @PostMapping("/api/tanques/{id}/descargar")
    void descargarTanque(@PathVariable("id") Long id,
                         @RequestParam("litros") BigDecimal litros,
                         @RequestHeader("X-User-Id") Long idUsuario);

    @PostMapping("/api/aceites/{id}/aumentar-stock")
    void aumentarStock(@PathVariable("id") Long id,
                       @RequestParam("cantidad") Integer cantidad,
                       @RequestHeader("X-User-Id") Long idUsuario);

    @PostMapping("/api/tanques/{id}/cargar")
    void cargarTanque(@PathVariable("id") Long id,
                      @RequestParam("litros") BigDecimal litros,
                      @RequestHeader("X-User-Id") Long idUsuario);
}
