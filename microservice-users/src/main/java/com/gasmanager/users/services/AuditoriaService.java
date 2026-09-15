package com.gasmanager.users.services;

import com.gasmanager.users.entities.AuditoriaAccion;
import com.gasmanager.users.enums.TipoAccion;
import com.gasmanager.users.repositories.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaAccion registrar(
            Long idUsuario, TipoAccion tipo, String descripcion, String modulo, String origen){
        AuditoriaAccion a = new AuditoriaAccion();
        a.setIdUsuarioEjecutor(idUsuario);
        a.setTipoAccion(tipo);
        a.setDescripcion(descripcion);
        a.setModuloAfectado(modulo);
        a.setOrigen(origen);
        a.setFechaHora(LocalDateTime.now());
        return auditoriaRepository.save(a);
    }
    public List<AuditoriaAccion>listarTodas(){
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<AuditoriaAccion> listarPorUsuario(Long idUsuario){
        return auditoriaRepository.findByIdUsuarioEjecutor(idUsuario);
    }

    public List<AuditoriaAccion> listarPorRango(LocalDateTime inicio, LocalDateTime fin){
        return auditoriaRepository.findByFechaHoraBetween(inicio, fin);
    }
}
