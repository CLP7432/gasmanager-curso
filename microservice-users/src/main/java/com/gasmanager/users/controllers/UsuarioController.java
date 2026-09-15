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
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return usuarioService.autenticar(req.getCorreo(), req.getPassword())
                .map(u -> {
                    if (u.getRol() == null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("error", "Sin rol asignado"));
                    }
                    String token = jwtTokenProvider.generateToken(
                            u.getId(),
                            u.getCorreo(),
                            u.getRol().getNombreRol()
                    );
                    sesionService.iniciarSesion(u.getId(), token);

                    // Lista de códigos de permiso del rol del usuario
                    List<String> permisos = u.getRol().getPermisos().stream()
                            .map(p -> p.getCodigoPermiso())
                            .toList();

                    return ResponseEntity.ok(new LoginResponse(
                            token, u.getRol().getNombreRol(), u.getId(), u.getCorreo(), permisos
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas")));
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.crearUsuario(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @RequestBody Usuario usuario){
        try{
            return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuario));
        }catch (IllegalArgumentException e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id){
        return usuarioService.desactivarUsuario(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/validar-token")
    public ResponseEntity<Boolean> validarToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return ResponseEntity.ok(false);
        }
        String token = authHeader.substring(7);
        try{
            jwtTokenProvider.validate(token);
            return ResponseEntity.ok(true);
        }catch (Exception e){
            return ResponseEntity.ok(false);
        }
    }
    @GetMapping("/test")
    public ResponseEntity<String>test(){
        return ResponseEntity.ok("Backend funcionando");
    }
    @GetMapping("/publico")
    public ResponseEntity<String>publico(){
        return ResponseEntity.ok("Endpoint publico funcionando");
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Usuario>> listarActivos() {
        return ResponseEntity.ok(usuarioService.listarActivos());
    }

    @GetMapping("/bloqueados")
    public ResponseEntity<List<Usuario>> listarBloqueados() {
        return ResponseEntity.ok(usuarioService.listarUsuariosBloqueados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtener(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{correo}/requiere-reset")
    public ResponseEntity<Boolean> requiereReset(@PathVariable String correo) {
        return ResponseEntity.ok(usuarioService.requiereResetPassword(correo));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> req) {
        String nuevaPassword = req.get("nuevaPassword");
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La contrasela debe tener al menos 6 caracteres"));
        }
        boolean ok = usuarioService.desbloquearYResetearPassword(id, nuevaPassword);
        if (ok) {
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida y usuario desbloqueado"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @PostMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Long id){
        return usuarioService.activarUsuario(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

}
