package com.gasmanager.ventas.services;

import com.gasmanager.ventas.clients.InventarioClient;
import com.gasmanager.ventas.dto.AceiteInventarioDTO;
import com.gasmanager.ventas.dto.EntregarIslaDTO;
import com.gasmanager.ventas.dto.StockSurtidorItemDTO;
import com.gasmanager.ventas.dto.SurtidorAceiteDTO;
import com.gasmanager.ventas.dto.SurtidorAceiteItemDTO;
import com.gasmanager.ventas.dto.SurtidorAceiteItemEntradaDTO;
import com.gasmanager.ventas.dto.GuardarSurtidorAceiteDTO;
import com.gasmanager.ventas.entities.EntregaIsla;
import com.gasmanager.ventas.entities.SurtidorAceite;
import com.gasmanager.ventas.entities.SurtidorAceiteItem;
import com.gasmanager.ventas.exceptions.RecursoNoEncontradoException;
import com.gasmanager.ventas.repositories.EntregaIslaRepository;
import com.gasmanager.ventas.repositories.SurtidorAceiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SurtidorAceiteService {

    private static final BigDecimal UMBRAL_ALERTA = new BigDecimal("3");

    private final SurtidorAceiteRepository surtidorAceiteRepository;
    private final EntregaIslaRepository entregaIslaRepository;
    private final InventarioClient inventarioClient;

    public SurtidorAceiteDTO aDTO(SurtidorAceite s) {
        return SurtidorAceiteDTO.builder()
                .id(s.getId())
                .despachadorId(s.getDespachadorId())
                .despachadorNombre(s.getDespachadorNombre())
                .dispensarioId(s.getDispensarioId())
                .nombre(s.getNombre())
                .activo(s.getActivo())
                .items(s.getItems().stream().map(i -> SurtidorAceiteItemDTO.builder()
                        .id(i.getId())
                        .aceiteId(i.getAceiteId())
                        .aceiteNombre(i.getAceiteNombre())
                        .categoria(i.getCategoria())
                        .stockActual(i.getStockActual())
                        .stockInicioTurno(i.getStockInicioTurno())
                        .precioVenta(i.getPrecioVenta())
                        .build()).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<SurtidorAceiteDTO> listar() {
        return surtidorAceiteRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public SurtidorAceiteDTO obtener(Long id) {
        return aDTO(surtidorAceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Surtidor de aceites no encontrado con id: " + id)));
    }

    @Transactional
    public SurtidorAceiteDTO guardar(GuardarSurtidorAceiteDTO dto) {
        if (dto.getDispensarioId() != null
                && surtidorAceiteRepository.findByDispensarioIdAndActivoTrue(dto.getDispensarioId()).isPresent()) {
            throw new IllegalArgumentException("La isla ya tiene un dispensario de aceites registrado");
        }
        SurtidorAceite surtidor = SurtidorAceite.builder()
                .despachadorId(dto.getDespachadorId())
                .despachadorNombre(dto.getDespachadorNombre())
                .dispensarioId(dto.getDispensarioId())
                .nombre(dto.getNombre())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
        if (dto.getItems() != null) {
            for (SurtidorAceiteItemEntradaDTO in : dto.getItems()) {
                AceiteInventarioDTO aceite = inventarioClient.obtenerAceite(in.getAceiteId());
                BigDecimal stock = in.getStockActual() != null ? in.getStockActual() : BigDecimal.ZERO;
                surtidor.getItems().add(SurtidorAceiteItem.builder()
                        .surtidor(surtidor)
                        .aceiteId(aceite.getId())
                        .aceiteNombre(aceite.getNombre())
                        .categoria(aceite.getTipoAceite())
                        .stockActual(stock)
                        .stockInicioTurno(stock)
                        .precioVenta(aceite.getPrecioVenta())
                        .build());
            }
        }
        return aDTO(surtidorAceiteRepository.save(surtidor));
    }

    @Transactional
    public SurtidorAceiteDTO actualizar(Long id, GuardarSurtidorAceiteDTO dto) {
        SurtidorAceite surtidor = surtidorAceiteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Surtidor de aceites no encontrado con id: " + id));
        surtidor.setDespachadorId(dto.getDespachadorId());
        surtidor.setDespachadorNombre(dto.getDespachadorNombre());
        surtidor.setDispensarioId(dto.getDispensarioId());
        surtidor.setNombre(dto.getNombre());
        if (dto.getActivo() != null) surtidor.setActivo(dto.getActivo());

        surtidor.getItems().clear();
        if (dto.getItems() != null) {
            for (SurtidorAceiteItemEntradaDTO in : dto.getItems()) {
                AceiteInventarioDTO aceite = inventarioClient.obtenerAceite(in.getAceiteId());
                BigDecimal stock = in.getStockActual() != null ? in.getStockActual() : BigDecimal.ZERO;
                surtidor.getItems().add(SurtidorAceiteItem.builder()
                        .surtidor(surtidor)
                        .aceiteId(aceite.getId())
                        .aceiteNombre(aceite.getNombre())
                        .categoria(aceite.getTipoAceite())
                        .stockActual(stock)
                        .stockInicioTurno(stock)
                        .precioVenta(aceite.getPrecioVenta())
                        .build());
            }
        }
        return aDTO(surtidorAceiteRepository.save(surtidor));
    }

    @Transactional
    public void entregarIsla(EntregarIslaDTO dto) {
        SurtidorAceite surtidor = surtidorAceiteRepository.findById(dto.getSurtidorAceiteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Surtidor de aceites no encontrado con id: " + dto.getSurtidorAceiteId()));
        SurtidorAceiteItem item = surtidor.getItems().stream()
                .filter(i -> Objects.equals(i.getAceiteId(), dto.getAceiteId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El aceite no está asignado al surtidor"));

        if (dto.getCantidad() == null || dto.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }

        item.setStockActual(item.getStockActual() != null ? item.getStockActual().add(dto.getCantidad()) : dto.getCantidad());

        try {
            inventarioClient.disminuirStock(dto.getAceiteId(), dto.getCantidad().intValue(),
                    dto.getUsuarioId() != null ? dto.getUsuarioId() : 0L);
        } catch (Exception e) {
            throw new IllegalArgumentException("No hay suficiente stock del aceite " + item.getAceiteNombre() + " en bodega");
        }

        entregaIslaRepository.save(EntregaIsla.builder()
                .surtidorAceiteId(surtidor.getId())
                .aceiteId(item.getAceiteId())
                .aceiteNombre(item.getAceiteNombre())
                .cantidad(dto.getCantidad())
                .turnoId(dto.getTurnoId())
                .usuarioId(dto.getUsuarioId())
                .fecha(LocalDateTime.now())
                .build());

        surtidorAceiteRepository.save(surtidor);
    }

    @Transactional(readOnly = true)
    public List<StockSurtidorItemDTO> stockConAlertas(Long surtidorId) {
        SurtidorAceite surtidor = surtidorAceiteRepository.findById(surtidorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Surtidor de aceites no encontrado con id: " + surtidorId));
        List<StockSurtidorItemDTO> resultado = new ArrayList<>();
        for (SurtidorAceiteItem item : surtidor.getItems()) {
            BigDecimal stock = item.getStockActual() != null ? item.getStockActual() : BigDecimal.ZERO;
            boolean alerta = stock.compareTo(UMBRAL_ALERTA) <= 0;
            resultado.add(StockSurtidorItemDTO.builder()
                    .aceiteId(item.getAceiteId())
                    .aceiteNombre(item.getAceiteNombre())
                    .categoria(item.getCategoria())
                    .stockActual(stock)
                    .precioVenta(precioVigenteAceite(item))
                    .alerta(alerta)
                    .mensaje(alerta ? "Bajo inventario: quedan " + stock + " unidades. Reabastecer." : null)
                    .build());
        }
        return resultado;
    }

    private BigDecimal precioVigenteAceite(SurtidorAceiteItem item) {
        try {
            AceiteInventarioDTO aceite = inventarioClient.obtenerAceite(item.getAceiteId());
            if (aceite != null && aceite.getPrecioVenta() != null) return aceite.getPrecioVenta();
        } catch (Exception ignore) {
        }
        return item.getPrecioVenta() != null ? item.getPrecioVenta() : BigDecimal.ZERO;
    }
}
