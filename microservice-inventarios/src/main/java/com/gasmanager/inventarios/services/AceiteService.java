package com.gasmanager.inventarios.services;

import com.gasmanager.inventarios.dto.AceiteDTO;
import com.gasmanager.inventarios.entities.Aceite;
import com.gasmanager.inventarios.exceptions.RecursoNoEncontradoException;
import com.gasmanager.inventarios.repositories.AceiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AceiteService {

    private final AceiteRepository aceiteRepository;

    public AceiteDTO aDTO (Aceite aceite){
        return AceiteDTO.builder()
                .id(aceite.getId())
                .codigo(aceite.getCodigo())
                .nombre(aceite.getNombre())
                .descripcion(aceite.getDescripcion())
                .marca(aceite.getMarca())
                .tipoAceite(aceite.getTipoAceite())
                .presentacion(aceite.getPresentacion())
                .unidadesPorCaja(aceite.getUnidadesPorCaja())
                .precioCompra(aceite.getPrecioCompra())
                .precioVenta(aceite.getPrecioVenta())
                .stockActual(aceite.getStockActual())
                .stockMinimo(aceite.getStockMinimo())
                .stockMaximo(aceite.getStockMaximo())
                .ubicacion(aceite.getUbicacion())
                .activo(aceite.getActivo())
                .build();
    }
    public Aceite aEntidad(AceiteDTO dto){
        return Aceite.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .marca(dto.getMarca())
                .tipoAceite(dto.getTipoAceite())
                .presentacion(dto.getPresentacion())
                .unidadesPorCaja(dto.getUnidadesPorCaja() != null ? dto.getUnidadesPorCaja() : 12)
                .precioCompra(dto.getPrecioCompra() != null ? dto.getPrecioCompra() : BigDecimal.ZERO)
                .precioVenta(dto.getPrecioVenta() != null ? dto.getPrecioVenta() : BigDecimal.ZERO)
                .stockActual(dto.getStockActual() != null ? dto.getStockActual() : 0)
                .stockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 5)
                .stockMaximo(dto.getStockMaximo() != null ? dto.getStockMaximo() : 50)
                .ubicacion(dto.getUbicacion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }
    @Transactional(readOnly = true)
    public List<AceiteDTO> listar(){
        return aceiteRepository.findAll().stream().map(this::aDTO).toList();
    }
    @Transactional(readOnly = true)
    public List<AceiteDTO> listarActivos(){
        return aceiteRepository.findByActivoTrue().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public AceiteDTO obtenerPorId(Long id){
        return aDTO(aceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aceite no encontrado con id: " + id)));
    }

    @Transactional
    public AceiteDTO crear (AceiteDTO dto, Long idUsuario){
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()) {
            dto.setCodigo(generarCodigoAceite());
        }
        validarCodigoUnico(dto.getCodigo(), null);
        Aceite aceite = aEntidad(dto);
        aceite.setCreatedBy(idUsuario);
        aceite.setUpdatedBy(idUsuario);
        return aDTO(aceiteRepository.save(aceite));
    }
    @Transactional
    public AceiteDTO actualizar(Long id, AceiteDTO dto, Long idUsuario){
        Aceite aceite = aceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aceite no encontrado con id: " + id));
        validarCodigoUnico(dto.getCodigo(), id);
        aceite.setCodigo(dto.getCodigo());
        aceite.setNombre(dto.getNombre());
        aceite.setDescripcion(dto.getDescripcion());
        aceite.setMarca(dto.getMarca());
        aceite.setTipoAceite(dto.getTipoAceite());
        aceite.setPresentacion(dto.getPresentacion());
        aceite.setUnidadesPorCaja(dto.getUnidadesPorCaja() != null ? dto.getUnidadesPorCaja() : aceite.getUnidadesPorCaja());
        if (dto.getPrecioCompra() != null) aceite.setPrecioCompra(dto.getPrecioCompra());
        if (dto.getPrecioVenta() != null) aceite.setPrecioVenta(dto.getPrecioVenta());
        aceite.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : aceite.getStockMinimo());
        aceite.setStockMaximo(dto.getStockMaximo() != null ? dto.getStockMaximo() : aceite.getStockMaximo());
        aceite.setUbicacion(dto.getUbicacion());
        aceite.setUpdatedBy(idUsuario);
        return aDTO(aceiteRepository.save(aceite));
    }
    @Transactional
    public void actualizarPrecio(Long id, BigDecimal precioVenta, Long idUsuario){
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor o igual a 0");
        }
        Aceite aceite = aceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aceite no encontrado con id: " + id));
        aceite.setPrecioVenta(precioVenta);
        aceite.setUpdatedBy(idUsuario);
        aceiteRepository.save(aceite);
    }
    @Transactional
    public void toggleActivo(Long id, Long idUsuario){
        Aceite aceite = aceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aceite no encontrado con id: " + id));
        aceite.setActivo(!aceite.getActivo());
        aceite.setUpdatedBy(idUsuario);
        aceiteRepository.save(aceite);
    }

    @Transactional
    public void aumentarStock(long id, Integer cantidad, Long idUsuario){
        if(cantidad == null || cantidad <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        Aceite aceite = aceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aceite no encontrado con id: " + id));
        aceite.setStockActual(aceite.getStockActual() + cantidad);
        aceite.setUpdatedBy(idUsuario);
        aceiteRepository.save(aceite);
    }
    @Transactional
    public void disminuirStock(Long id, Integer cantidad, Long idUsuario){
        if(cantidad == null || cantidad <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        Aceite aceite = aceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aceite no encontrado con id: " + id));
        if(aceite.getStockActual() - cantidad <aceite.getStockMinimo()){
            throw new IllegalArgumentException("Stock insuficiente: quedaría por debajo del mínimo permitido");
        }
        aceite.setStockActual(aceite.getStockActual() - cantidad);
        aceite.setUpdatedBy(idUsuario);
        aceiteRepository.save(aceite);
    }
    @Transactional(readOnly = true)
    public List<AceiteDTO> listarStockBajo(){
        return aceiteRepository.findByActivoTrue().stream()
                .filter((a -> a.getStockActual() <= a.getStockMinimo()))
                .map(this::aDTO)
                .toList();
    }
    private void validarCodigoUnico(String codigo, Long idExcluido){
        aceiteRepository.findByCodigo(codigo)
                .filter(a -> !a.getId().equals(idExcluido))
                .ifPresent(a -> {
                    throw new IllegalArgumentException("Ya existe un aceite con el código: " + codigo);
                });
    }

    private String generarCodigoAceite() {
        long count = aceiteRepository.count() + 1;
        return "ACE-" + String.format("%04d", count);
    }
}
