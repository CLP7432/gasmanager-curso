package com.gasmanager.inventarios.services;

import com.gasmanager.inventarios.dto.CompraDTO;
import com.gasmanager.inventarios.dto.CompraDetalleDTO;
import com.gasmanager.inventarios.dto.DescargaPipaDTO;
import com.gasmanager.inventarios.dto.PendienteCargaDTO;
import com.gasmanager.inventarios.entities.*;
import com.gasmanager.inventarios.exceptions.RecursoNoEncontradoException;
import com.gasmanager.inventarios.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CompraService {

    private static final BigDecimal IVA_TASA = new BigDecimal("0.16");
    public static final BigDecimal CAPACIDAD_PIPA_LITROS = new BigDecimal("30000");

    private final CompraRepository compraRepository;
    private final CompraDetalleRepository compraDetalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final AceiteRepository aceiteRepository;
    private final CombustibleRepository combustibleRepository;
    private final PrecioHistoricoRepository precioHistoricoRepository;
    private final TanqueRepository tanqueRepository;

    public CompraDetalleDTO aDTO(CompraDetalle d) {
        return CompraDetalleDTO.builder()
                .id(d.getId())
                .tipoProducto(d.getTipoProducto())
                .aceiteId(d.getAceite() != null ? d.getAceite().getId() : null)
                .combustibleId(d.getCombustible() != null ? d.getCombustible().getId() : null)
                .productoNombre(d.getTipoProducto().equals("ACEITE")
                        ? (d.getAceite() != null ? d.getAceite().getNombre() : null)
                        : (d.getCombustible() != null ? d.getCombustible().getNombre() : null))
                .presentacion(d.getPresentacion())
                .unidadesPorCaja(d.getUnidadesPorCaja())
                .cajas(d.getCajas())
                .piezas(d.getPiezas())
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .subtotal(d.getSubtotal())
                .litrosDescargados(d.getLitrosDescargados())
                .build();
    }

    public CompraDTO aDTO(Compra c) {
        List<CompraDetalle> detalles = c.getDetalles();
        boolean tieneCombustible = detalles.stream()
                .anyMatch(d -> "COMBUSTIBLE".equals(d.getTipoProducto()));
        String estadoDescarga = "SIN_COMBUSTIBLE";
        if (tieneCombustible) {
            boolean hayPendiente = detalles.stream()
                    .filter(d -> "COMBUSTIBLE".equals(d.getTipoProducto()))
                    .anyMatch(d -> litrosPendientes(d).compareTo(BigDecimal.ZERO) > 0);
            estadoDescarga = hayPendiente ? "PENDIENTE" : "DESCARGADA";
        }

        return CompraDTO.builder()
                .id(c.getId())
                .folioFactura(c.getFolioFactura())
                .proveedorId(c.getProveedor() != null ? c.getProveedor().getId() : null)
                .proveedorRazonSocial(c.getProveedor() != null ? c.getProveedor().getRazonSocial() : null)
                .proveedorRfc(c.getProveedor() != null ? c.getProveedor().getRfc() : null)
                .fechaFactura(c.getFechaFactura() != null ? c.getFechaFactura().toString() : null)
                .fechaRegistro(c.getFechaRegistro() != null ? c.getFechaRegistro().toString() : null)
                .tipoCompra(c.getTipoCompra())
                .subtotal(c.getSubtotal())
                .iva(c.getIva())
                .total(c.getTotal())
                .activo(c.getActivo())
                .estadoDescarga(estadoDescarga)
                .detalles(detalles.stream().map(this::aDTO).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CompraDTO> listar() {
        return compraRepository.findAllByOrderByFechaRegistroDesc().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CompraDTO> listarPorProveedor(Long proveedorId) {
        return compraRepository.findByProveedorIdOrderByFechaRegistroDesc(proveedorId).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public CompraDTO obtenerPorId(Long id) {
        return aDTO(compraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra no encontrada con id: " + id)));
    }

    @Transactional
    public CompraDTO registrar(CompraDTO dto, Long idUsuario) {
        if (dto.getFolioFactura() != null && !dto.getFolioFactura().isBlank()
                && compraRepository.findByFolioFactura(dto.getFolioFactura()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una compra con el folio: " + dto.getFolioFactura());
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe incluir al menos un producto");
        }
        Proveedor proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado con id: " + dto.getProveedorId()));

        Compra compra = Compra.builder()
                .folioFactura(dto.getFolioFactura())
                .proveedor(proveedor)
                .fechaFactura(dto.getFechaFactura() != null ? LocalDate.parse(dto.getFechaFactura()) : null)
                .fechaRegistro(LocalDateTime.now())
                .tipoCompra(proveedor.getTipoProveedor() != null ? proveedor.getTipoProveedor().name() : "VARIOS")
                .activo(true)
                .createdBy(idUsuario)
                .updatedBy(idUsuario)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CompraDetalleDTO detalleDTO : dto.getDetalles()) {
            CompraDetalle detalle = procesarDetalle(compra, detalleDTO, dto.getFolioFactura(), idUsuario);
            compra.getDetalles().add(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());
        }
        compra.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        compra.setIva(subtotal.multiply(IVA_TASA).setScale(2, RoundingMode.HALF_UP));
        compra.setTotal(compra.getSubtotal().add(compra.getIva()));
        return aDTO(compraRepository.save(compra));
    }

    private CompraDetalle procesarDetalle(Compra compra, CompraDetalleDTO dto, String folioFactura, Long idUsuario) {
        BigDecimal precio = dto.getPrecioUnitario();
        BigDecimal cantidad;

        if ("ACEITE".equalsIgnoreCase(dto.getTipoProducto())) {
            Aceite aceite = aceiteRepository.findById(dto.getAceiteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Aceite no encontrado con id: " + dto.getAceiteId()));
            int unidadesPorCaja = dto.getUnidadesPorCaja() != null ? dto.getUnidadesPorCaja() : (aceite.getUnidadesPorCaja() != null ? aceite.getUnidadesPorCaja() : 1);
            int cajas = dto.getCajas() != null ? dto.getCajas() : 0;
            int piezas = dto.getPiezas() != null ? dto.getPiezas() : 0;
            int totalUnidades = cajas * unidadesPorCaja + piezas;
            if (totalUnidades <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio unitario del aceite debe ser mayor a 0");
            }
            if (aceite.getPrecioVenta() != null && precio.compareTo(aceite.getPrecioVenta()) > 0) {
                throw new IllegalArgumentException("El precio de compra de " + aceite.getNombre()
                        + " ($" + precio + ") no debe ser mayor al precio de venta ($" + aceite.getPrecioVenta() + ")");
            }
            cantidad = BigDecimal.valueOf(totalUnidades);
            if (precio != null && precio.compareTo(BigDecimal.ZERO) > 0) {
                aceite.setPrecioCompra(precio);
            }
            aceite.setStockActual(aceite.getStockActual() + totalUnidades);
            aceite.setUpdatedBy(idUsuario);
            aceiteRepository.save(aceite);

            return CompraDetalle.builder()
                    .compra(compra)
                    .tipoProducto("ACEITE")
                    .aceite(aceite)
                    .presentacion(aceite.getPresentacion())
                    .unidadesPorCaja(unidadesPorCaja)
                    .cajas(cajas)
                    .piezas(piezas)
                    .cantidad(cantidad)
                    .precioUnitario(precio)
                    .subtotal(precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP))
                    .build();
        }

        Combustible combustible = combustibleRepository.findById(dto.getCombustibleId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Combustible no encontrado con id: " + dto.getCombustibleId()));
        cantidad = dto.getCantidad();
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad (litros) debe ser mayor a 0");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        if (combustible.getPrecioActual() != null && precio.compareTo(combustible.getPrecioActual()) > 0) {
            throw new IllegalArgumentException("El precio de compra de " + combustible.getNombre()
                    + " ($" + precio + ") no debe ser mayor al precio de venta ($" + combustible.getPrecioActual() + ")");
        }
        if (precio != null && precio.compareTo(BigDecimal.ZERO) > 0) {
            // La compra actualiza el COSTO, nunca el precio de venta
            combustible.setPrecioCompra(precio);
            combustibleRepository.save(combustible);
        }
        return CompraDetalle.builder()
                .compra(compra)
                .tipoProducto("COMBUSTIBLE")
                .combustible(combustible)
                .presentacion("Granel")
                .unidadesPorCaja(null)
                .cajas(0)
                .piezas(0)
                .cantidad(cantidad)
                .precioUnitario(precio)
                .subtotal(precio.multiply(cantidad).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Transactional(readOnly = true)
    public List<PendienteCargaDTO> listarPendientesCarga() {
        return compraRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .flatMap(compra -> compra.getDetalles().stream()
                        .filter(d -> "COMBUSTIBLE".equals(d.getTipoProducto()))
                        .filter(d -> litrosPendientes(d).compareTo(BigDecimal.ZERO) > 0)
                        .map(d -> pendienteCargaDTO(compra, d)))
                .sorted(Comparator.comparing(PendienteCargaDTO::getDetalleId))
                .toList();
    }

    @Transactional
    public PendienteCargaDTO descargarPipa(DescargaPipaDTO dto, Long idUsuario) {
        CompraDetalle detalle = compraDetalleRepository.findById(dto.getDetalleId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Detalle de compra no encontrado con id: " + dto.getDetalleId()));
        if (!"COMBUSTIBLE".equals(detalle.getTipoProducto())) {
            throw new IllegalArgumentException("Solo se pueden descargar productos de tipo combustible");
        }
        BigDecimal litros = dto.getLitros();
        BigDecimal pendientes = litrosPendientes(detalle);
        if (litros.compareTo(pendientes) > 0) {
            throw new IllegalArgumentException(
                    "Solo resta descargar " + pendientes.setScale(0) + " L de " + detalle.getCombustible().getNombre());
        }
        Combustible combustible = detalle.getCombustible();
        Tanque tanque = tanqueRepository.findFirstByTipoCombustibleAndActivoTrueOrderByIdAsc(combustible.getTipo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay un tanque activo para " + combustible.getNombre()
                                + ". Crea uno con capacidad de al menos 40,000 L"));
        BigDecimal nuevoStock = tanque.getStockLitros().add(litros);
        if (nuevoStock.compareTo(tanque.getCapacidadLitros()) > 0) {
            BigDecimal espacio = tanque.getCapacidadLitros().subtract(tanque.getStockLitros());
            throw new IllegalArgumentException(
                    "El tanque " + tanque.getNombre() + " solo tiene espacio para " + espacio.setScale(0) + " L");
        }
        tanque.setStockLitros(nuevoStock);
        tanque.setUpdatedBy(idUsuario);
        tanqueRepository.save(tanque);

        BigDecimal descargados = detalle.getLitrosDescargados() != null
                ? detalle.getLitrosDescargados() : BigDecimal.ZERO;
        detalle.setLitrosDescargados(descargados.add(litros));
        return pendienteCargaDTO(detalle.getCompra(), detalle);
    }

    private BigDecimal litrosPendientes(CompraDetalle detalle) {
        BigDecimal descargados = detalle.getLitrosDescargados() != null
                ? detalle.getLitrosDescargados() : BigDecimal.ZERO;
        return detalle.getCantidad().subtract(descargados);
    }

    private PendienteCargaDTO pendienteCargaDTO(Compra compra, CompraDetalle detalle) {
        Combustible combustible = detalle.getCombustible();
        Tanque tanque = combustible != null
                ? tanqueRepository.findFirstByTipoCombustibleAndActivoTrueOrderByIdAsc(combustible.getTipo()).orElse(null)
                : null;
        BigDecimal espacio = tanque != null
                ? tanque.getCapacidadLitros().subtract(tanque.getStockLitros()) : null;
        return PendienteCargaDTO.builder()
                .compraId(compra.getId())
                .folioFactura(compra.getFolioFactura())
                .fechaFactura(compra.getFechaFactura() != null ? compra.getFechaFactura().toString() : null)
                .tipoCompra(compra.getTipoCompra())
                .detalleId(detalle.getId())
                .combustibleId(combustible != null ? combustible.getId() : null)
                .combustibleNombre(combustible != null ? combustible.getNombre() : null)
                .tipoCombustible(combustible != null && combustible.getTipo() != null ? combustible.getTipo().name() : null)
                .litrosComprados(detalle.getCantidad())
                .litrosDescargados(detalle.getLitrosDescargados() != null ? detalle.getLitrosDescargados() : BigDecimal.ZERO)
                .litrosPendientes(litrosPendientes(detalle))
                .tanqueId(tanque != null ? tanque.getId() : null)
                .tanqueNombre(tanque != null ? tanque.getNombre() : null)
                .tanqueCapacidad(tanque != null ? tanque.getCapacidadLitros() : null)
                .tanqueEspacioDisponible(espacio)
                .build();
    }

    @Transactional
    public void toggleActivo(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Compra no encontrada con id: " + id));
        compra.setActivo(!compra.getActivo());
        compraRepository.save(compra);
    }

    @Transactional(readOnly = true)
    public List<CompraDTO> listarPorMes(int anio, int mes){
        LocalDateTime inicio = LocalDate.of(anio, mes, 1).atStartOfDay();
        LocalDateTime fin = inicio.plusMonths(1).minusNanos(1);
        return compraRepository.findByFechaRegistroBetween(inicio, fin).stream()
                .map(this::aDTO).toList();
    }
}