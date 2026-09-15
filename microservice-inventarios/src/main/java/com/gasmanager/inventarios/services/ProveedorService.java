package com.gasmanager.inventarios.services;

import com.gasmanager.inventarios.dto.ProveedorDTO;
import com.gasmanager.inventarios.entities.Proveedor;
import com.gasmanager.inventarios.enums.TipoProveedor;
import com.gasmanager.inventarios.exceptions.RecursoNoEncontradoException;
import com.gasmanager.inventarios.repositories.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorDTO aDTO(Proveedor p) {
        return ProveedorDTO.builder()
                .id(p.getId())
                .rfc(p.getRfc())
                .razonSocial(p.getRazonSocial())
                .nombreComercial(p.getNombreComercial())
                .contacto(p.getContacto())
                .telefono(p.getTelefono())
                .email(p.getEmail())
                .tipoProveedor(p.getTipoProveedor() != null ? p.getTipoProveedor().name() : null)
                .activo(p.getActivo())
                .build();
    }

    private Proveedor aEntidad(ProveedorDTO dto) {
        return Proveedor.builder()
                .rfc(dto.getRfc())
                .razonSocial(dto.getRazonSocial())
                .nombreComercial(dto.getNombreComercial())
                .contacto(dto.getContacto())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .tipoProveedor(TipoProveedor.fromString(dto.getTipoProveedor()))
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listar() {
        return proveedorRepository.findAll().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProveedorDTO> listarActivos() {
        return proveedorRepository.findByActivoTrue().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProveedorDTO obtenerPorId(Long id) {
        return aDTO(proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id: " + id)));
    }

    @Transactional
    public ProveedorDTO crear(ProveedorDTO dto, Long idUsuario) {
        if (proveedorRepository.existsByRfc(dto.getRfc())) {
            throw new IllegalArgumentException("Ya existe un proveedor con el RFC: " + dto.getRfc());
        }
        Proveedor proveedor = aEntidad(dto);
        proveedor.setCreatedBy(idUsuario);
        proveedor.setUpdatedBy(idUsuario);
        return aDTO(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorDTO actualizar(Long id, ProveedorDTO dto, Long idUsuario) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id: " + id));
        proveedorRepository.findByRfc(dto.getRfc())
                .filter(p -> !p.getId().equals(id))
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Ya existe un proveedor con el RFC: " + dto.getRfc());
                });
        proveedor.setRfc(dto.getRfc());
        proveedor.setRazonSocial(dto.getRazonSocial());
        proveedor.setNombreComercial(dto.getNombreComercial());
        proveedor.setContacto(dto.getContacto());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        if (dto.getTipoProveedor() != null) {
            proveedor.setTipoProveedor(TipoProveedor.fromString(dto.getTipoProveedor()));
        }
        proveedor.setUpdatedBy(idUsuario);
        return aDTO(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void toggleActivo(Long id, Long idUsuario) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id: " + id));
        proveedor.setActivo(!proveedor.getActivo());
        proveedor.setUpdatedBy(idUsuario);
        proveedorRepository.save(proveedor);
    }
}