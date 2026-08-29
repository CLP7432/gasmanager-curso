package com.gasmanager.users.controllers;

import com.gasmanager.users.entities.Permiso;
import com.gasmanager.users.entities.Rol;
import com.gasmanager.users.services.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @PostMapping
    public ResponseEntity<Rol> crear(@RequestBody Rol rol){
        return ResponseEntity.ok(rolService.crearRol(rol));
    }

    @GetMapping
    public ResponseEntity<List<Rol>> listar(){
        return ResponseEntity.ok(rolService.listarRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> obtener(@PathVariable Long id){
        return rolService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/permisos")
    public ResponseEntity<Rol> asignarPermiso(
            @PathVariable Long id, @RequestBody Permiso permiso){
        return ResponseEntity.ok(rolService.asignarPermiso(id, permiso));
    }
}
