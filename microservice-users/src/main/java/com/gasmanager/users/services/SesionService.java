package com.gasmanager.users.services;

import com.gasmanager.users.entities.SesionUsuario;
import com.gasmanager.users.repositories.SesionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SesionService {

    private final SesionRepository sesionRepository;

    public SesionUsuario iniciarSesion(Long idUsuario, String token){
        SesionUsuario s = new SesionUsuario();
        s.setIdUsuario(idUsuario);
        s.setToken(token);
        s.setFechaInicio(LocalDateTime.now());
        s.setFechaExpiracion(LocalDateTime.now().plusHours(2));
        s.setActivo(true);
        return sesionRepository.save(s);
    }

    public void cerrarSesion(String token){
        sesionRepository.findByToken(token).ifPresent(s ->{
            s.setActivo(false);
            sesionRepository.save(s);
        });
    }

    public boolean validarToken(String token){
        return sesionRepository.findByToken(token)
                .map(SesionUsuario::getActivo)
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }
}
