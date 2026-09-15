package com.gasmanager.users.controllers;

import com.gasmanager.users.entities.AuditoriaAccion;
import com.gasmanager.users.services.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<List<AuditoriaAccion>> listar(){
        return ResponseEntity.ok(auditoriaService.listarTodas());
    }
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<AuditoriaAccion>> porUsuario(@PathVariable Long idUsuario){
        return ResponseEntity.ok(auditoriaService.listarPorUsuario(idUsuario));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<AuditoriaAccion>> porRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fin){
        return ResponseEntity.ok(auditoriaService.listarPorRango(inicio, fin));
    }
}
