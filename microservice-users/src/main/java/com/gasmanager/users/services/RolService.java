package com.gasmanager.users.services;

import com.gasmanager.users.entities.Permiso;
import com.gasmanager.users.entities.Rol;
import com.gasmanager.users.enums.TipoAccion;
import com.gasmanager.users.repositories.PermisoRepository;
import com.gasmanager.users.repositories.RolRepository;
import com.gasmanager.users.repositories.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public Rol crearRol(Rol rol){
        if(rolRepository.existsByNombreRol(rol.getNombreRol())){
            throw  new ResponseStatusException(HttpStatus.CONFLICT, "Rol ya existe");
        }
        Rol rolCreado = rolRepository.save(rol);
        auditoriaService.registrar(
                rolCreado.getId(),
                TipoAccion.CREAR,
                "Rol creado: " + rolCreado.getNombreRol(),
                "Roles",
                "Sistema"
        );
        return rolCreado;
    }
    
    public List<Rol> listarRoles(){
        return rolRepository.findAll();
    }

    public Optional<Rol> obtenerPorId(Long id){
        return rolRepository.findById(id);
    }
    public List<Rol> listarRolesActivos(){
        return rolRepository.findAll().stream()
                .filter(rol -> Boolean.TRUE.equals(rol.getActivo()))
                .toList();
    }
    public Rol actualizarRol(Long id, Rol rolActualizado){
        Rol rolExistente = rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
        if(rolActualizado.getNombreRol() != null){
            rolExistente.setNombreRol(rolActualizado.getNombreRol());
        }
        if(rolActualizado.getDescripcion() != null){
            rolExistente.setDescripcion(rolActualizado.getDescripcion());
        }
        if(rolActualizado.getPermisos() != null){
            rolExistente.setPermisos(rolActualizado.getPermisos());
        }
        if(rolActualizado.getActivo() != null){
            rolExistente.setActivo(rolActualizado.getActivo());
        }
        Rol rolGuardado = rolRepository.save(rolExistente);
        auditoriaService.registrar(
                id,
                TipoAccion.ACTUALIZAR,
                "Rol actualizado: " + rolGuardado.getNombreRol(),
                "Roles",
                "Sistema"
        );
        return rolGuardado;
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
    public boolean eliminarRol(Long id){
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
        if(usuarioRepository.countByRolId(id) > 0){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede eliminar el rol porque está asignado a uno o más usuarios");
        }
        auditoriaService.registrar(
                id,
                TipoAccion.ELIMINAR,
                "Rol eliminado: " + rol.getNombreRol(),
                "Roles",
                "Sistema"
        );
        rolRepository.deleteById(id);
        return true;
    }
}
