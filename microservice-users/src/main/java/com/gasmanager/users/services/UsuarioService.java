package com.gasmanager.users.services;

import com.gasmanager.users.entities.Rol;
import com.gasmanager.users.entities.Usuario;
import com.gasmanager.users.enums.EstadoUsuario;
import com.gasmanager.users.enums.TipoAccion;
import com.gasmanager.users.repositories.RolRepository;
import com.gasmanager.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final AuditoriaService auditoriaService;

    //Crear
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        if (usuario.getRol() != null && usuario.getRol().getId() != null) {
            Rol rolDB = rolRepository.findById(usuario.getRol().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Rol no existe"));
            usuario.setRol(rolDB);
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado(false);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        Usuario usuarioCreado = usuarioRepository.save(usuario);
        auditoriaService.registrar(
                usuarioCreado.getId(),
                TipoAccion.CREAR,
                "Usuario creado: " + usuarioCreado.getNombre(),
                "Usuarios",
                "Sistema"
        );
        return usuarioCreado;
    }

    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }
        if (usuarioActualizado.getRol() != null) {
            usuarioExistente.setRol(usuarioActualizado.getRol());
        }
        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }
        Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);
        auditoriaService.registrar(
                id,
                TipoAccion.ACTUALIZAR,
                "Usuario actualizado: " + usuarioGuardado.getCorreo(),
                "Usuarios",
                "Sistema"
        );
        return usuarioGuardado;

    }

    public boolean desactivarUsuario(Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return false;
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(false);
        usuario.setEstado(EstadoUsuario.INACTIVO);

        usuarioRepository.save(usuario);
        auditoriaService.registrar(
                id,
                TipoAccion.ELIMINAR,
                "Usuario desactivado: " + usuario.getCorreo(),
                "Usuarios",
                "Sistema"
        );
        return true;
    }

    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findAllByActivoTrue();
    }

    public List<Usuario> listarUsuariosBloqueados() {
        return usuarioRepository.findByBloqueadoTrue();
    }

    //Autenticar - Bloque a los 3 intentos
    public Optional<Usuario> autenticar(String correo, String password) {
        Optional<Usuario> opt = usuarioRepository.findByCorreo(correo);

        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Usuario u = opt.get();
        if (Boolean.TRUE.equals(u.getBloqueado())) {
            log.warn("Intento login bloqueado: {}", correo);
            return Optional.empty();
        }
        if (passwordEncoder.matches(password, u.getPassword())) {
            u.setIntentosFallidos(0);
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
            auditoriaService.registrar(
                    u.getId(),
                    TipoAccion.VALIDAR,
                    "Login exitoso: " + u.getCorreo(),
                    "Usuarios",
                    "WEB"
            );
            return Optional.of(u);
        } else {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            if (u.getIntentosFallidos() >= 3) {
                u.setBloqueado(true);
                u.setEstado(EstadoUsuario.BLOQUEADO);
                auditoriaService.registrar(
                        u.getId(),
                        TipoAccion.VALIDAR,
                        "USUARIO BLOQUEADO - 3 intentos fallidos",
                        "Usuarios",
                        "WEB"
                );
            }
            usuarioRepository.save(u);
            auditoriaService.registrar(
                    u.getId(),
                    TipoAccion.VALIDAR,
                    "Login fallido - Intento #" + u.getIntentosFallidos(),
                    "Usuarios",
                    "WEB"
            );
            return Optional.empty();
        }
    }

    public boolean desbloquearYResetearPassword(Long idUsuario, String nuevaPassword) {
        Optional<Usuario> opt = usuarioRepository.findById(idUsuario);

        if (opt.isEmpty()) return false;

        Usuario u = opt.get();
        u.setPassword(passwordEncoder.encode(nuevaPassword));
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        u.setEstado(EstadoUsuario.ACTIVO);
        u.setActivo(true);
        usuarioRepository.save(u);
        return true;
    }

    public boolean requiereResetPassword(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .map(u -> Boolean.TRUE.equals(u.getBloqueado()) && u.getIntentosFallidos() >= 3)
                .orElse(false);
    }

    public boolean activarUsuario(Long idUsuario) {
        Optional<Usuario> opt = usuarioRepository.findById(idUsuario);
        if (opt.isEmpty()) {
            return false;
        }
        Usuario u = opt.get();
        u.setActivo(true);
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        u.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(u);
        return true;
    }
}
