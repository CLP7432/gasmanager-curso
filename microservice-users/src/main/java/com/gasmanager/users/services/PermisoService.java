package com.gasmanager.users.services;

import com.gasmanager.users.entities.Permiso;
import com.gasmanager.users.enums.TipoAccion;
import com.gasmanager.users.repositories.PermisoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final AuditoriaService auditoriaService;

    public Permiso crearPermiso(Permiso permiso){
        if(permisoRepository.existsByCodigoPermiso(permiso.getCodigoPermiso())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El permiso ya existe");
        }
        Permiso permisoCreado = permisoRepository.save(permiso);

        auditoriaService.registrar(
                null,
                TipoAccion.CREAR,
                "Permiso creado: " + permisoCreado.getNombrePermiso(),
                "Permisos",
                "Sistema"
        );

        return permisoCreado;
    }
    public List<Permiso>listarPermisos(){
        return permisoRepository.findAll();
    }
    public Permiso buscarPorCodigo(String codigo){
        return permisoRepository.findByCodigoPermiso(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Permiso no encontrado"));
    }
    public Optional<Permiso> obtenerPorId(Long id){
        return permisoRepository.findById(id);
    }

    public boolean eliminarPermiso(Long id){
        if(permisoRepository.existsById(id)){
            permisoRepository.deleteById(id);
            auditoriaService.registrar(
                    null,
                    TipoAccion.ELIMINAR,
                    "Permiso eliminado",
                    "Permisos",
                    "Sistema"
            );
            return true;
        }
        return false;
    }

    public Permiso actualizarPermiso(Long id, Permiso permisoActualizado){
        Permiso permisoExistente = permisoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Permiso no encontrado"));
        if(permisoActualizado.getCodigoPermiso() != null){
            permisoExistente.setCodigoPermiso(permisoActualizado.getCodigoPermiso());
        }
        if(permisoActualizado.getNombrePermiso() != null){
            permisoExistente.setNombrePermiso(permisoActualizado.getNombrePermiso());
        }
        if(permisoActualizado.getDescripcion() != null){
            permisoExistente.setDescripcion(permisoActualizado.getDescripcion());
        }
        if(permisoActualizado.getActivo() != null){
            permisoExistente.setActivo(permisoActualizado.getActivo());
        }
        Permiso permisoGuardado = permisoRepository.save(permisoExistente);
        auditoriaService.registrar(
                null,
                TipoAccion.ACTUALIZAR,
                "Permiso actualizado: " + permisoGuardado.getNombrePermiso(),
                "Permisos",
                "Sistema"
        );
        return permisoGuardado;
    }

}
