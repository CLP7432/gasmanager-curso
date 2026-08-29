package com.gasmanager.users.controllers;

import com.gasmanager.users.dto.LoginRequest;
import com.gasmanager.users.dto.LoginResponse;
import com.gasmanager.users.entities.Usuario;
import com.gasmanager.users.security.JwtTokenProvider;
import com.gasmanager.users.services.SesionService;
import com.gasmanager.users.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SesionService sesionService;

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody LoginRequest req){
        return usuarioService.autenticar(req.getCorreo(), req.getPassword())
                .map(u -> {
                    if(u.getRol() == null){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Sin rol asignado"));
                    }
                    String token = jwtTokenProvider.generateToken(
                            u.getId(),
                            u.getCorreo(),
                            u.getRol().getNombreRol()
                    );
                    sesionService.iniciarSesion(u.getId(), token);

                    return ResponseEntity.ok(new LoginResponse(
                            token, u.getRol().getNombreRol(), u.getId(), u.getCorreo()
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas")));
    }
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario){
        return ResponseEntity.ok(usuarioService.crearUsuario(usuario));
    }
    @GetMapping
    public ResponseEntity<List<Usuario>> listar(){
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtener(@PathVariable Long id){
        return usuarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

}
