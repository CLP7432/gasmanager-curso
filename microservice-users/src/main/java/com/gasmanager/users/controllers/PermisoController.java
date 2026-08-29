package com.gasmanager.users.controllers;

import com.gasmanager.users.entities.Permiso;
import com.gasmanager.users.services.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    @PostMapping
    public Permiso crear(@RequestBody Permiso permiso){
        return permisoService.crearPermiso(permiso);
    }
    @GetMapping
    public List<Permiso> listar(){
        return permisoService.listarPermisos();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Permiso> obtener(@PathVariable Long id){
        return permisoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        return permisoService.eliminarPermiso(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
