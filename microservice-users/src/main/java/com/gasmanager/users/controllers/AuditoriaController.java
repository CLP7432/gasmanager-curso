package com.gasmanager.users.controllers;

import com.gasmanager.users.entities.AuditoriaAccion;
import com.gasmanager.users.services.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
