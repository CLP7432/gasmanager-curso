package com.gasmanager.users.services;

import com.gasmanager.users.entities.Permiso;
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

    public Permiso crearPermiso(Permiso permiso){
        if(permisoRepository.existsByCodigoPermiso(permiso.getCodigoPermiso())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El permiso ya existe");
        }
        return permisoRepository.save(permiso);
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
            return true;
        }
        return false;
    }

}
