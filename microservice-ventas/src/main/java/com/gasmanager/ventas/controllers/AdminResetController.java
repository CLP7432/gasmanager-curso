package com.gasmanager.ventas.controllers;

import com.gasmanager.ventas.services.AdminResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminResetController {

    private final AdminResetService adminResetService;

    @PostMapping("/reset-base")
    public ResponseEntity<Map<String, Object>> resetBase() {
        adminResetService.resetBase();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Base de datos reiniciada a cero. Se conservaron catálogos, precios y el usuario admin.",
                "proximosIds", adminResetService.proximosIds()
        ));
    }

    @GetMapping("/proximos-ids")
    public ResponseEntity<List<Map<String, Object>>> proximosIds() {
        return ResponseEntity.ok(adminResetService.proximosIds());
    }
}