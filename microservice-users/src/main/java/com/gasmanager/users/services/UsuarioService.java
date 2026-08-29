package com.gasmanager.users.services;

import com.gasmanager.users.entities.Usuario;
import com.gasmanager.users.enums.EstadoUsuario;
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

    //Crear
    public Usuario crearUsuario(Usuario usuario){
        if(usuarioRepository.existsByCorreo(usuario.getCorreo())){
            throw new IllegalArgumentException("El correo ya esta registrado");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado(false);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }
    public Optional<Usuario> obtenerPorId(Long id){
        return usuarioRepository.findById(id);
    }
    public List<Usuario> listarTodos(){
        return usuarioRepository.findAll();
    }
    //Autenticar - Bloque a los 3 intentos
    public Optional<Usuario> autenticar(String correo, String password){
        Optional<Usuario> opt = usuarioRepository.findByCorreo(correo);

        if(opt.isEmpty()){
            return Optional.empty();
        }
        Usuario u = opt.get();
        if(Boolean.TRUE.equals(u.getBloqueado())){
            log.warn("Intento login bloqueado: {}", correo);
            return Optional.empty();
        }
        if(passwordEncoder.matches(password, u.getPassword())){
            u.setIntentosFallidos(0);
            u.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(u);
            return Optional.of(u);
        }else{
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            if(u.getIntentosFallidos() >= 3){
                u.setBloqueado(true);
                u.setEstado(EstadoUsuario.BLOQUEADO);
            }
            usuarioRepository.save(u);
            return Optional.empty();
        }
    }
}
