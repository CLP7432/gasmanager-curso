package com.gasmanager.inventarios.services;

import com.gasmanager.inventarios.clients.UsuarioClient;
import com.gasmanager.inventarios.dto.CambioPrecioDTO;
import com.gasmanager.inventarios.dto.CombustibleDTO;
import com.gasmanager.inventarios.dto.PrecioHistoricoDTO;
import com.gasmanager.inventarios.entities.Combustible;
import com.gasmanager.inventarios.entities.PrecioHistorico;
import com.gasmanager.inventarios.enums.TipoCombustible;
import com.gasmanager.inventarios.exceptions.RecursoNoEncontradoException;
import com.gasmanager.inventarios.repositories.CombustibleRepository;
import com.gasmanager.inventarios.repositories.PrecioHistoricoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CombustibleService {

    private final CombustibleRepository combustibleRepository;
    private final PrecioHistoricoRepository precioHistoricoRepository;
    private final UsuarioClient usuarioClient;

    public CombustibleDTO aDTO(Combustible c) {
        return CombustibleDTO.builder()
                .id(c.getId())
                .tipo(c.getTipo() != null ? c.getTipo().name() : null)
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .precioActual(c.getPrecioActual())
                .precioCompra(c.getPrecioCompra())
                .fechaUltimoCambioPrecio(c.getFechaUltimoCambioPrecio())
                .activo(c.getActivo())
                .build();
    }

    public PrecioHistoricoDTO aDTOHistorico(PrecioHistorico ph) {
        return PrecioHistoricoDTO.builder()
                .id(ph.getId())
                .combustibleId(ph.getCombustible() != null ? ph.getCombustible().getId() : null)
                .tipoCombustible(ph.getCombustible() != null ? ph.getCombustible().getTipo().name() : null)
                .precioAnterior(ph.getPrecioAnterior())
                .precioNuevo(ph.getPrecioNuevo())
                .fechaCambio(ph.getFechaCambio())
                .motivoCambio(ph.getMotivoCambio())
                .cambiadoPor(ph.getCambiadoPor())
                .cambiadoPorId(ph.getCambiadoPorId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CombustibleDTO> listar() {
        return combustibleRepository.findAll().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CombustibleDTO> listarActivos() {
        return combustibleRepository.findByActivoTrue().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public CombustibleDTO obtenerPorId(Long id) {
        return aDTO(combustibleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Combustible no encontrado con id: " + id)));
    }

    @Transactional
    public CombustibleDTO crear(CombustibleDTO dto, Long idUsuario) {
        validarTipoUnico(dto.getTipo(), null);
        Combustible c = Combustible.builder()
                .tipo(TipoCombustible.fromString(dto.getTipo()))
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precioActual(dto.getPrecioActual())
                .precioCompra(dto.getPrecioCompra())
                .activo(true)
                .createdBy(idUsuario)
                .updatedBy(idUsuario)
                .build();
        if (c.getTipo() == null) {
            throw new IllegalArgumentException("Tipo de combustible inválido: " + dto.getTipo());
        }
        return aDTO(combustibleRepository.save(c));
    }

    @Transactional
    public CombustibleDTO actualizar(Long id, CombustibleDTO dto, Long idUsuario) {
        Combustible c = combustibleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Combustible no encontrado con id: " + id));
        validarTipoUnico(dto.getTipo(), id);
        c.setTipo(TipoCombustible.fromString(dto.getTipo()));
        c.setNombre(dto.getNombre());
        c.setDescripcion(dto.getDescripcion());
        c.setUpdatedBy(idUsuario);
        if (c.getTipo() == null) {
            throw new IllegalArgumentException("Tipo de combustible invalido: " + dto.getTipo());
        }
        return aDTO(combustibleRepository.save(c));
    }

    @Transactional
    public void toggleActivo(Long id, Long idUsuario) {
        Combustible c = combustibleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Combustible no encontrado con id: " + id));
        c.setActivo(!c.getActivo());
        c.setUpdatedBy(idUsuario);
        combustibleRepository.save(c);
    }

    @Transactional
    public CombustibleDTO cambiarPrecio(Long id, CambioPrecioDTO cambio, Long idUsuario) {
        Combustible c = combustibleRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Combustible no encontrado con id: " + id));
        BigDecimal precioAnterior = c.getPrecioActual();
        BigDecimal precioNuevo = cambio.getNuevoPrecio();

        if (precioAnterior != null && precioAnterior.compareTo(precioNuevo) == 0) {
            throw new IllegalArgumentException("El nuevo precio es igual al precio actual");
        }
        c.setPrecioActual(precioNuevo);
        c.setFechaUltimoCambioPrecio(LocalDateTime.now());
        c.setUpdatedBy(idUsuario);
        combustibleRepository.save(c);

        PrecioHistorico historico = PrecioHistorico.builder()
                .combustible(c)
                .precioAnterior(precioAnterior)
                .precioNuevo(precioNuevo)
                .motivoCambio(cambio.getMotivo())
                .cambiadoPor(obtenerNombreUsuario(idUsuario))
                .cambiadoPorId(idUsuario)
                .fechaCambio(LocalDateTime.now())
                .build();
        precioHistoricoRepository.save(historico);
        return aDTO(c);
    }

    @Transactional(readOnly = true)
    public List<PrecioHistoricoDTO> listarHistorial(Long combustibleId) {
        return precioHistoricoRepository.findByCombustibleIdOrderByFechaCambioDesc(combustibleId).stream()
                .map(this::aDTOHistorico)
                .toList();
    }

    private void validarTipoUnico(String tipo, Long idExcluido) {
        TipoCombustible tipoEnum = TipoCombustible.fromString(tipo);
        if (tipoEnum == null) {
            throw new IllegalArgumentException("Tipo de combustible inválido: " + tipo);
        }
        combustibleRepository.findByTipo(tipoEnum)
                .filter(c -> !c.getId().equals(idExcluido))
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Ya existe un combustible del tipo: " + tipo);
                });
    }
    private String obtenerNombreUsuario(Long idUsuario){
        try{
            Map<String, Object> usuario = usuarioClient.obtenerPorId(idUsuario);
            return (String) usuario.get("nombre");
        }catch (Exception e){
            return "Usuario" + idUsuario;
        }
    }
}
