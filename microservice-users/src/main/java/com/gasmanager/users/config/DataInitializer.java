package com.gasmanager.users.config;

import com.gasmanager.users.entities.Permiso;
import com.gasmanager.users.entities.Rol;
import com.gasmanager.users.entities.Usuario;
import com.gasmanager.users.enums.EstadoUsuario;
import com.gasmanager.users.repositories.PermisoRepository;
import com.gasmanager.users.repositories.RolRepository;
import com.gasmanager.users.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.initial-data.enabled:true}")
    private boolean initialDataEnabled;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if(!initialDataEnabled) return;

        if(usuarioRepository.count() > 0){
            System.out.println("======YA EXISTEN USUARIOS - NO SE INICIALIZA=====");
            return;
        }
        System.out.println("=====INICIALIZANDO DATOS=====");
        crearPermisos();
        crearRoles();
        crearAdmin();

        System.out.println("=====ADMIN CREADO admin@gasmanager.com / Cambiami123! ===");
    }
    private void crearPermisos(){
        if(permisoRepository.count() > 0) return;

        Permiso[] permisos = {
                new Permiso("USUARIO_CREAR", "Crear Usuario", "Permite crear usuarios"),
                new Permiso("USUARIO_LEER", "Leer Usuario", "Permite ver usuarios"),
                new Permiso("USUARIO_ACTUALIZAR", "Actualizar Usuario", "Permite modificar"),
                new Permiso("USUARIO_ELIMINAR", "Eliminar Usuario", "Permite desactivar"),
                new Permiso("ROL_CREAR", "Crear Rol", "Permite crear roles"),
                new Permiso("ROL_LEER", "Leer Rol", "Permite ver roles"),
                new Permiso("AUDITORIA_LEER", "Leer Auditoria", "Permite ver auditoria")
        };
        for(Permiso p : permisos){
            permisoRepository.save(p);
            System.out.println("Permiso: " + p.getCodigoPermiso());
        }
    }
    private void crearRoles(){
        if(rolRepository.count() > 0) return;

        Rol admin = new Rol("ADMIN", "Administrador con todos los permisos");
        permisoRepository.findAll().forEach(admin.getPermisos()::add);
        rolRepository.save(admin);
        System.out.println("Rol ADMIN");

        Rol usuario = new Rol("USUARIO", "Usuario estándar");
        permisoRepository.findByCodigoPermiso("USUARIO_LEER").ifPresent(usuario.getPermisos()::add);
        rolRepository.save(usuario);
        System.out.println("Rol USUARIO");
    }
    private void crearAdmin(){
        Rol adminRol = rolRepository.findByNombreRol("ADMIN")
                .orElseThrow(() -> new RuntimeException("No ADMIN"));
        Usuario admin = new Usuario();
        admin.setNombre("Administrador del Sistema");
        admin.setCorreo("admin@gasmanager.com");
        admin.setPassword(passwordEncoder.encode("Cambiami123!"));
        admin.setRol(adminRol);
        admin.setEstado(EstadoUsuario.ACTIVO);
        admin.setActivo(true);
        admin.setBloqueado(false);
        admin.setIntentosFallidos(0);
        admin.setFechaCreacion(LocalDateTime.now());

        usuarioRepository.save(admin);
    }
}
