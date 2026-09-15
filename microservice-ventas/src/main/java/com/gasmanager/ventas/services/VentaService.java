package com.gasmanager.ventas.services;

import com.gasmanager.ventas.clients.InventarioClient;
import com.gasmanager.ventas.dto.DetalleVentaDTO;
import com.gasmanager.ventas.dto.VentaDTO;
import com.gasmanager.ventas.entities.DetalleVenta;
import com.gasmanager.ventas.entities.Dispensario;
import com.gasmanager.ventas.entities.Manguera;
import com.gasmanager.ventas.entities.Turno;
import com.gasmanager.ventas.entities.Venta;
import com.gasmanager.ventas.enums.EstadoTurno;
import com.gasmanager.ventas.enums.EstadoVenta;
import com.gasmanager.ventas.enums.MetodoPago;
import com.gasmanager.ventas.exceptions.RecursoNoEncontradoException;
import com.gasmanager.ventas.repositories.CorteRepository;
import com.gasmanager.ventas.repositories.DispensarioRepository;
import com.gasmanager.ventas.repositories.MangueraRepository;
import com.gasmanager.ventas.repositories.TurnoRepository;
import com.gasmanager.ventas.repositories.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private static final BigDecimal IVA_TASA = new BigDecimal("0.16");
    private final VentaRepository ventaRepository;
    private final InventarioClient inventarioClient;
    private final TurnoRepository turnoRepository;
    private final DispensarioRepository dispensarioRepository;
    private final MangueraRepository mangueraRepository;
    private final CorteRepository corteRepository;

    public DetalleVentaDTO aDTO(DetalleVenta d) {
        return DetalleVentaDTO.builder()
                .id(d.getId())
                .tipoProducto(d.getTipoProducto())
                .productoId(d.getProductoId())
                .productoNombre(d.getProductoNombre())
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .subtotal(d.getSubtotal())
                .tanqueId(d.getTanqueId())
                .build();
    }

    public VentaDTO aDTO(Venta v) {
        return VentaDTO.builder()
                .id(v.getId())
                .folio(v.getFolio())
                .fechaHora(v.getFechaHora() != null ? v.getFechaHora().toString() : null)
                .metodoPago(v.getMetodoPago() != null ? v.getMetodoPago().name() : null)
                .estado(v.getEstado() != null ? v.getEstado().name() : null)
                .usuarioId(v.getUsuarioId())
                .subtotal(v.getSubtotal())
                .iva(v.getIva())
                .total(v.getTotal())
                .turnoId(v.getTurnoId())
                .despachadorId(v.getDespachadorId())
                .despachadorNombre(v.getDespachadorNombre())
                .dispensarioId(v.getDispensarioId())
                .mangueraId(v.getMangueraId())
                .detalles(v.getDetalles().stream().map(this::aDTO).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listar() {
        return ventaRepository.findAllByOrderByFechaHoraDesc().stream()
                .map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerPorId(Long id) {
        return aDTO(ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id)));
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerPorFolio(String folio) {
        return aDTO(ventaRepository.findByFolio(folio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con folio: " + folio)));
    }

    @Transactional
    public VentaDTO registrar(VentaDTO dto, Long idUsuario) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto");
        }
        if (dto.getTurnoId() == null) {
            throw new IllegalArgumentException("La venta requiere un turno activo");
        }
        Turno turno = turnoRepository.findById(dto.getTurnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado con id: " + dto.getTurnoId()));
        if (turno.getEstado() != EstadoTurno.ABIERTO) {
            throw new IllegalArgumentException("El turno " + turno.getCodigoTurno() + " está " + turno.getEstado() + ", no se pueden registrar ventas");
        }
        if (dto.getDispensarioId() != null
                && corteRepository.existsByTurnoIdAndDispensarioId(turno.getId(), dto.getDispensarioId())) {
            throw new IllegalArgumentException("El dispensario ya tiene corte en el turno " + turno.getCodigoTurno()
                    + ". Cierra el turno antes de registrar nuevas ventas.");
        }
        if (dto.getDispensarioId() != null) {
            Dispensario dispensario = dispensarioRepository.findById(dto.getDispensarioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + dto.getDispensarioId()));
            if (!Boolean.TRUE.equals(dispensario.getActivo())) {
                throw new IllegalArgumentException("El dispensario " + dispensario.getNombre() + " está inactivo");
            }
            if (dispensario.getDespachadorId() == null) {
                throw new IllegalArgumentException("El dispensario " + dispensario.getNombre() + " no tiene despachador asignado");
            }
            if (dto.getDespachadorId() == null || !dto.getDespachadorId().equals(dispensario.getDespachadorId())) {
                throw new IllegalArgumentException("El despachador de la venta no coincide con el asignado al dispensario " + dispensario.getNombre());
            }
        }
        if (dto.getMangueraId() != null) {
            Manguera manguera = mangueraRepository.findById(dto.getMangueraId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Manguera no encontrada con id: " + dto.getMangueraId()));
            if (manguera.getCara() == null || manguera.getCara().getDispensario() == null
                    || dto.getDispensarioId() == null
                    || !manguera.getCara().getDispensario().getId().equals(dto.getDispensarioId())) {
                throw new IllegalArgumentException("La manguera no pertenece al dispensario indicado");
            }
        }

        Venta venta = Venta.builder()
                .folio(generarFolio())
                .fechaHora(LocalDateTime.now())
                .metodoPago(MetodoPago.valueOf(dto.getMetodoPago() != null ? dto.getMetodoPago().toUpperCase() : "EFECTIVO"))
                .estado(dto.getMetodoPago() != null && dto.getMetodoPago().equalsIgnoreCase("CREDITO")
                        ? EstadoVenta.PENDIENTE : EstadoVenta.COMPLETADA)
                .usuarioId(dto.getUsuarioId() != null ? dto.getUsuarioId() : idUsuario)
                .turnoId(dto.getTurnoId())
                .despachadorId(dto.getDespachadorId())
                .despachadorNombre(dto.getDespachadorNombre())
                .dispensarioId(dto.getDispensarioId())
                .mangueraId(dto.getMangueraId())
                .build();
        BigDecimal subtotal = BigDecimal.ZERO;
        for(DetalleVentaDTO detalleDTO : dto.getDetalles()){
            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .tipoProducto(detalleDTO.getTipoProducto())
                    .productoId(detalleDTO.getProductoId())
                    .productoNombre(detalleDTO.getProductoNombre())
                    .cantidad(detalleDTO.getCantidad())
                    .precioUnitario(detalleDTO.getPrecioUnitario())
                    .subtotal(detalleDTO.getPrecioUnitario()
                            .multiply(detalleDTO.getCantidad()))
                    .tanqueId(detalleDTO.getTanqueId())
                    .build();
            venta.getDetalles().add(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());

            if("ACEITE".equalsIgnoreCase(detalleDTO.getTipoProducto())){
                inventarioClient.disminuirStock(
                        detalleDTO.getProductoId(),
                        detalleDTO.getCantidad().intValue(),
                        idUsuario != null ? idUsuario : 0L
                );
            }
            if ("COMBUSTIBLE".equalsIgnoreCase(detalleDTO.getTipoProducto())) {
                inventarioClient.descargarTanque(
                        detalleDTO.getTanqueId(),
                        detalleDTO.getCantidad(),
                        idUsuario != null ? idUsuario : 0L);
            }

        }
        venta.setTotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        venta.setIva(venta.getTotal()
                .multiply(IVA_TASA)
                .divide(IVA_TASA.add(BigDecimal.ONE), 2, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP));
        venta.setSubtotal(venta.getTotal().subtract(venta.getIva()));

        return aDTO(ventaRepository.save(venta));
    }
    @Transactional
    public VentaDTO cancelar(Long id, Long idUsuario){
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada con id: " + id));
        if(venta.getEstado() == EstadoVenta.CANCELADA){
            throw new IllegalArgumentException("La venta " + venta.getFolio() + " ya está cancelada");
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            if ("ACEITE".equalsIgnoreCase(detalle.getTipoProducto())) {
                inventarioClient.aumentarStock(
                        detalle.getProductoId(),
                        detalle.getCantidad().intValue(),
                        idUsuario != null ? idUsuario : 0L);
            }
            if ("COMBUSTIBLE".equalsIgnoreCase(detalle.getTipoProducto()) && detalle.getTanqueId() != null) {
                inventarioClient.cargarTanque(
                        detalle.getTanqueId(),
                        detalle.getCantidad(),
                        idUsuario != null ? idUsuario : 0L);
            }
        }
        venta.setEstado(EstadoVenta.CANCELADA);
        return aDTO(ventaRepository.save(venta));
    }
    @Transactional(readOnly = true)
    public List<VentaDTO> reporteMensual(int anio, int mes) {
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1);
        return ventaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin)
                .stream().map(this::aDTO).toList();
    }
    @Transactional(readOnly = true)
    public List<VentaDTO> porTurno(Long turnoId) {
        return ventaRepository.findByTurnoIdOrderByFechaHoraAsc(turnoId)
                .stream().map(this::aDTO).toList();
    }


    private String generarFolio(){
        Long numero = ventaRepository.findFirstByOrderByIdDesc()
                .map((v -> Long.parseLong(v.getFolio().substring(4))))
                .orElse(0L) + 1;
        return String.format("VEN-%05d", numero);
    }
}