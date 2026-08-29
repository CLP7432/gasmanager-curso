package com.gasmanager.users.services;

import com.gasmanager.users.entities.Permiso;
import com.gasmanager.users.entities.Rol;
import com.gasmanager.users.repositories.PermisoRepository;
import com.gasmanager.users.repositories.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public Rol crearRol(Rol rol){
        if(rolRepository.existsByNombreRol(rol.getNombreRol())){
            throw  new ResponseStatusException(HttpStatus.CONFLICT, "Rol ya existe");
        }
        return rolRepository.save(rol);
    }
    
    public List<Rol> listarRoles(){
        return rolRepository.findAll();
    }

    public Optional<Rol> obtenerPorId(Long id){
        return rolRepository.findById(id);
    }

    public Rol asignarPermiso(Long idRol, Permiso permiso){
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,"Rol no encontrado"));
        Permiso  permisoDB = permisoRepository.findById(permiso.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"Permiso no encontrado"));
        if(rol.getPermisos().contains(permisoDB)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Permiso ya asignado");
        }
        rol.getPermisos().add(permisoDB);
        return rolRepository.save(rol);
    }
}
