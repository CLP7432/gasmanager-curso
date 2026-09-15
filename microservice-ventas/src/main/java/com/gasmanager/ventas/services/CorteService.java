package com.gasmanager.ventas.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gasmanager.ventas.clients.ClientesClient;
import com.gasmanager.ventas.clients.InventarioClient;
import com.gasmanager.ventas.clients.NominaClient;
import com.gasmanager.ventas.dto.AceiteInventarioDTO;
import com.gasmanager.ventas.dto.CorteAceiteDTO;
import com.gasmanager.ventas.dto.CorteDTO;
import com.gasmanager.ventas.dto.CorteResumenDTO;
import com.gasmanager.ventas.dto.GenerarCorteAceiteDTO;
import com.gasmanager.ventas.dto.GenerarCorteDTO;
import com.gasmanager.ventas.dto.LineaResumenDTO;
import com.gasmanager.ventas.dto.CreditoClienteDTO;
import com.gasmanager.ventas.dto.CreditoCorteDTO;
import com.gasmanager.ventas.dto.NotaCreditoCorteDTO;
import com.gasmanager.ventas.dto.NotaCreditoClienteDTO;
import com.gasmanager.ventas.dto.CrearNotaCreditoRequestDTO;
import com.gasmanager.ventas.dto.RegistrarIncidenciaDTO;
import com.gasmanager.ventas.entities.Corte;
import com.gasmanager.ventas.entities.CorteAceite;
import com.gasmanager.ventas.entities.DetalleVenta;
import com.gasmanager.ventas.entities.Dispensario;
import com.gasmanager.ventas.entities.EntregaIsla;
import com.gasmanager.ventas.entities.SurtidorAceite;
import com.gasmanager.ventas.entities.SurtidorAceiteItem;
import com.gasmanager.ventas.entities.Turno;
import com.gasmanager.ventas.entities.Venta;
import com.gasmanager.ventas.enums.EstadoCorte;
import com.gasmanager.ventas.enums.EstadoVenta;
import com.gasmanager.ventas.enums.MetodoPago;
import com.gasmanager.ventas.exceptions.RecursoNoEncontradoException;
import com.gasmanager.ventas.repositories.CorteAceiteRepository;
import com.gasmanager.ventas.repositories.CorteRepository;
import com.gasmanager.ventas.repositories.DispensarioRepository;
import com.gasmanager.ventas.repositories.EntregaIslaRepository;
import com.gasmanager.ventas.repositories.SurtidorAceiteRepository;
import com.gasmanager.ventas.repositories.TurnoRepository;
import com.gasmanager.ventas.repositories.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CorteService {

    private final CorteRepository corteRepository;
    private final TurnoRepository turnoRepository;
    private final DispensarioRepository dispensarioRepository;
    private final VentaRepository ventaRepository;
    private final NominaClient nominaClient;
    private final InventarioClient inventarioClient;
    private final ClientesClient clientesClient;
    private final SurtidorAceiteRepository surtidorAceiteRepository;
    private final CorteAceiteRepository corteAceiteRepository;
    private final EntregaIslaRepository entregaIslaRepository;
    private final ObjectMapper objectMapper;

    public CorteDTO aDTO(Corte c) {
        return CorteDTO.builder()
                .id(c.getId())
                .codigoCorte(c.getCodigoCorte())
                .turnoId(c.getTurnoId())
                .dispensarioId(c.getDispensarioId())
                .dispensarioNombre(c.getDispensarioNombre())
                .despachadorId(c.getDespachadorId())
                .despachadorNombre(c.getDespachadorNombre())
                .numeroVentas(c.getNumeroVentas())
                .totalLitros(c.getTotalLitros())
                .totalVentas(c.getTotalVentas())
                .totalAceites(c.getTotalAceites())
                .notasCreditoTotal(c.getNotasCreditoTotal())
                .creditosTotal(c.getCreditosTotal())
                .esperadoEfectivo(c.getEsperadoEfectivo())
                .esperadoTarjeta(c.getEsperadoTarjeta())
                .esperadoTransferencia(c.getEsperadoTransferencia())
                .esperadoCredito(c.getEsperadoCredito())
                .efectivoRecibido(c.getEfectivoRecibido())
                .tarjetaRecibido(c.getTarjetaRecibido())
                .transferenciaRecibido(c.getTransferenciaRecibido())
                .diferenciaEfectivo(c.getDiferenciaEfectivo())
                .observaciones(c.getObservaciones())
                .estado(c.getEstado() != null ? c.getEstado().name() : null)
                .validadoPor(c.getValidadoPor())
                .validadoFecha(c.getValidadoFecha() != null ? c.getValidadoFecha().toString() : null)
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .aceites(corteAceiteRepository.findByCorteId(c.getId()).stream()
                        .map(a -> CorteAceiteDTO.builder()
                                .id(a.getId())
                                .surtidorAceiteId(a.getSurtidorAceiteId())
                                .aceiteId(a.getAceiteId())
                                .aceiteNombre(a.getAceiteNombre())
                                .categoria(a.getCategoria())
                                .recibidoTotal(a.getRecibidoTotal())
                                .sobrante(a.getSobrante())
                                .vendidos(a.getVendidos())
                                .precioVenta(a.getPrecioVenta())
                                .importe(a.getImporte())
                                .build())
                        .toList())
                .notasCredito(deserializar(c.getNotasJson(), new TypeReference<List<NotaCreditoCorteDTO>>() {}))
                .creditos(deserializar(c.getCreditosJson(), new TypeReference<List<CreditoCorteDTO>>() {}))
                .build();
    }

    private String serializar(Object valor) {
        if (valor == null) return null;
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T deserializar(String json, TypeReference<T> tipo) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, tipo);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<CorteDTO> listar(String estado) {
        if (estado != null && !estado.isBlank()) {
            return corteRepository.findByEstadoOrderByIdDesc(EstadoCorte.valueOf(estado.toUpperCase()))
                    .stream().map(this::aDTO).toList();
        }
        return corteRepository.findAllByOrderByIdDesc().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CorteDTO> listarPorTurno(Long turnoId) {
        return corteRepository.findByTurnoIdOrderByIdDesc(turnoId).stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public CorteDTO obtener(Long id) {
        return aDTO(corteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Corte no encontrado con id: " + id)));
    }

    @Transactional(readOnly = true)
    public CorteResumenDTO resumen(Long turnoId, Long dispensarioId) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado con id: " + turnoId));
        Dispensario dispensario = dispensarioRepository.findById(dispensarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + dispensarioId));

        List<Venta> ventas = ventaRepository.findByTurnoIdAndDispensarioIdOrderByFechaHoraAsc(turno.getId(), dispensario.getId());

        Map<String, LineaResumenDTO> combustibles = new LinkedHashMap<>();
        Map<String, LineaResumenDTO> aceites = new LinkedHashMap<>();
        List<NotaCreditoCorteDTO> notas = new ArrayList<>();

        BigDecimal totalLitros = BigDecimal.ZERO;
        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal esperadoEfectivo = BigDecimal.ZERO;
        BigDecimal esperadoTarjeta = BigDecimal.ZERO;
        BigDecimal esperadoTransferencia = BigDecimal.ZERO;
        BigDecimal esperadoCredito = BigDecimal.ZERO;
        BigDecimal notasCreditoTotal = BigDecimal.ZERO;
        int numeroVentas = 0;

        for (Venta v : ventas) {
            if (v.getEstado() == EstadoVenta.CANCELADA) {
                notas.add(NotaCreditoCorteDTO.builder()
                        .folio(v.getFolio())
                        .importe(v.getTotal())
                        .build());
                notasCreditoTotal = notasCreditoTotal.add(v.getTotal());
                continue;
            }
            numeroVentas++;
            totalVentas = totalVentas.add(v.getTotal());
            if (v.getDetalles() != null) {
                for (DetalleVenta d : v.getDetalles()) {
                    if ("COMBUSTIBLE".equalsIgnoreCase(d.getTipoProducto())) {
                        totalLitros = totalLitros.add(d.getCantidad());
                        LineaResumenDTO linea = combustibles.computeIfAbsent(d.getProductoNombre(),
                                k -> LineaResumenDTO.builder().producto(k).cantidad(BigDecimal.ZERO).importe(BigDecimal.ZERO).build());
                        linea.setCantidad(linea.getCantidad().add(d.getCantidad()));
                        linea.setImporte(linea.getImporte().add(d.getSubtotal()));
                    } else if ("ACEITE".equalsIgnoreCase(d.getTipoProducto())) {
                        LineaResumenDTO linea = aceites.computeIfAbsent(d.getProductoNombre(),
                                k -> LineaResumenDTO.builder().producto(k).cantidad(BigDecimal.ZERO).importe(BigDecimal.ZERO).build());
                        linea.setCantidad(linea.getCantidad().add(d.getCantidad()));
                        linea.setImporte(linea.getImporte().add(d.getSubtotal()));
                    }
                }
            }
            if (v.getMetodoPago() == MetodoPago.EFECTIVO) {
                esperadoEfectivo = esperadoEfectivo.add(v.getTotal());
            } else if (v.getMetodoPago() == MetodoPago.TARJETA_CREDITO || v.getMetodoPago() == MetodoPago.TARJETA_DEBITO) {
                esperadoTarjeta = esperadoTarjeta.add(v.getTotal());
            } else if (v.getMetodoPago() == MetodoPago.TRANSFERENCIA) {
                esperadoTransferencia = esperadoTransferencia.add(v.getTotal());
            } else if (v.getMetodoPago() == MetodoPago.CREDITO) {
                esperadoCredito = esperadoCredito.add(v.getTotal());
            }
        }

        List<CorteAceiteDTO> aceitesCorte = aceitesCorteDelSurtidor(dispensario.getId(), turno.getId());
        BigDecimal aceiteImporte = aceitesCorte.stream()
                .map(CorteAceiteDTO::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try {
            List<NotaCreditoClienteDTO> notasCliente = clientesClient.listarNotasPorFechas(
                    turno.getFechaTurno(), turno.getFechaTurno());
            if (notasCliente != null) {
                for (NotaCreditoClienteDTO nc : notasCliente) {
                    if (nc.getOrigenCorte() != null) continue;
                    if (nc.getItems() == null) continue;
                    for (NotaCreditoClienteDTO.ItemNotaCreditoClienteDTO item : nc.getItems()) {
                        if (!"COMBUSTIBLE".equalsIgnoreCase(item.getTipo())) continue;
                        String fam = familiaCombustible(item.getProducto());
                        if (fam == null) continue;
                        BigDecimal importe = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;
                        if (importe.compareTo(BigDecimal.ZERO) <= 0) continue;
                        notas.add(NotaCreditoCorteDTO.builder()
                                .folio(nc.getNumero())
                                .fecha(nc.getFechaCarga())
                                .cliente(nc.getClienteNombre())
                                .tipoCombustible(fam)
                                .litros(item.getCantidad())
                                .importe(importe)
                                .build());
                        notasCreditoTotal = notasCreditoTotal.add(importe);
                    }
                }
            }
        } catch (Exception e) {
            // Si microservice-clientes no está disponible, continuar sin las notas
        }

        return CorteResumenDTO.builder()
                .turnoId(turno.getId())
                .dispensarioId(dispensario.getId())
                .dispensarioNombre(dispensario.getNombre())
                .despachadorNombre(dispensario.getDespachadorNombre())
                .numeroVentas(numeroVentas)
                .totalLitros(totalLitros)
                .totalVentas(totalVentas)
                .combustibles(new ArrayList<>(combustibles.values()))
                .aceites(new ArrayList<>(aceites.values()))
                .aceitesCorte(aceitesCorte)
                .aceiteImporte(aceiteImporte)
                .notasCredito(notas)
                .notasCreditoTotal(notasCreditoTotal)
                .esperadoEfectivo(esperadoEfectivo)
                .esperadoTarjeta(esperadoTarjeta)
                .esperadoTransferencia(esperadoTransferencia)
                .esperadoCredito(esperadoCredito)
                .netoEfectivo(esperadoEfectivo
                        .add(aceiteImporte)
                        .subtract(notasCreditoTotal))
                .build();
    }

    // El surtidor de aceites pertenece a la isla (dispensarioId), no al
    // despachador: el despachador rota por turno y ya no se guarda en el surtidor.
    private List<CorteAceiteDTO> aceitesCorteDelSurtidor(Long dispensarioId, Long turnoId) {
        if (dispensarioId == null) return new ArrayList<>();
        SurtidorAceite surtidor = surtidorAceiteRepository.findByDispensarioIdAndActivoTrue(dispensarioId).orElse(null);
        if (surtidor == null) return new ArrayList<>();
        List<EntregaIsla> entregas = entregaIslaRepository.findBySurtidorAceiteIdAndTurnoIdOrderByFechaAsc(surtidor.getId(), turnoId);
        List<CorteAceiteDTO> resultado = new ArrayList<>();
        for (SurtidorAceiteItem item : surtidor.getItems()) {
            BigDecimal inicio = item.getStockInicioTurno() != null ? item.getStockInicioTurno() : BigDecimal.ZERO;
            BigDecimal entregasTurno = entregas.stream()
                    .filter(e -> Objects.equals(e.getAceiteId(), item.getAceiteId()))
                    .map(EntregaIsla::getCantidad)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal recibido = inicio.add(entregasTurno);
            BigDecimal precio = precioVigenteAceite(item.getAceiteId(), item.getPrecioVenta());
            resultado.add(CorteAceiteDTO.builder()
                    .surtidorAceiteId(surtidor.getId())
                    .aceiteId(item.getAceiteId())
                    .aceiteNombre(item.getAceiteNombre())
                    .categoria(item.getCategoria())
                    .recibidoTotal(recibido)
                    .sobrante(BigDecimal.ZERO)
                    .vendidos(recibido)
                    .precioVenta(precio)
                    .importe(recibido.multiply(precio))
                    .build());
        }
        return resultado;
    }

    private BigDecimal precioVigenteAceite(Long aceiteId, BigDecimal respaldo) {
        try {
            AceiteInventarioDTO aceite = inventarioClient.obtenerAceite(aceiteId);
            if (aceite != null && aceite.getPrecioVenta() != null) return aceite.getPrecioVenta();
        } catch (Exception ignore) {
        }
        return respaldo != null ? respaldo : BigDecimal.ZERO;
    }

    @Transactional
    public CorteDTO generar(GenerarCorteDTO dto) {
        Turno turno = turnoRepository.findById(dto.getTurnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado con id: " + dto.getTurnoId()));
        Dispensario dispensario = dispensarioRepository.findById(dto.getDispensarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + dto.getDispensarioId()));
        if (corteRepository.existsByTurnoIdAndDispensarioId(turno.getId(), dispensario.getId())) {
            throw new IllegalArgumentException("El dispensario " + dispensario.getNombre() + " ya tiene corte en el turno " + turno.getCodigoTurno());
        }
        Map.Entry<Long, String> despachador = despachadorDelTurno(turno.getId(), dispensario);
        if (despachador.getKey() == null) {
            throw new IllegalArgumentException("No se puede generar el corte del dispensario " + dispensario.getNombre()
                    + ": no tiene despachador asignado ni ventas con despachador en el turno " + turno.getCodigoTurno());
        }

        CalculoCorte calc = calcular(turno, dispensario, dto.getNotasCredito(), dto.getCreditos(), dto.getAceites(), dto.getObservaciones(), null);

        BigDecimal efectivoRecibido = dto.getEfectivoRecibido() != null ? dto.getEfectivoRecibido() : BigDecimal.ZERO;
        BigDecimal tarjetaRecibido = dto.getTarjetaRecibido() != null ? dto.getTarjetaRecibido() : BigDecimal.ZERO;
        BigDecimal transferenciaRecibido = dto.getTransferenciaRecibido() != null ? dto.getTransferenciaRecibido() : BigDecimal.ZERO;

        BigDecimal baseCaja = calc.esperadoEfectivo()
                .add(calc.aceiteImporte())
                .subtract(calc.notasCreditoTotal())
                .subtract(calc.creditoImporteTotal());
        BigDecimal netoEfectivo = baseCaja.subtract(tarjetaRecibido).subtract(transferenciaRecibido);

        Corte corte = Corte.builder()
                .codigoCorte(generarCodigo())
                .turnoId(turno.getId())
                .dispensarioId(dispensario.getId())
                .dispensarioNombre(dispensario.getNombre())
                .despachadorId(despachador.getKey())
                .despachadorNombre(despachador.getValue())
                .numeroVentas(calc.numeroVentas())
                .totalLitros(calc.totalLitros())
                .totalVentas(calc.totalVentas())
                .totalAceites(calc.aceiteImporte())
                .notasCreditoTotal(calc.notasCreditoTotal())
                .creditosTotal(calc.creditoImporteTotal())
                .esperadoEfectivo(baseCaja)
                .esperadoTarjeta(calc.esperadoTarjeta())
                .esperadoTransferencia(calc.esperadoTransferencia())
                .esperadoCredito(calc.esperadoCredito())
                .efectivoRecibido(efectivoRecibido)
                .tarjetaRecibido(tarjetaRecibido)
                .transferenciaRecibido(transferenciaRecibido)
                .diferenciaEfectivo(netoEfectivo.subtract(efectivoRecibido))
                .observaciones(calc.observaciones())
                .notasJson(serializar(dto.getNotasCredito()))
                .creditosJson(serializar(dto.getCreditos()))
                .estado(EstadoCorte.PENDIENTE)
                .build();
        Corte guardado = corteRepository.save(corte);
        aplicarAceites(calc.surtidorAceite(), calc.filasAceite(), calc.aceitesSobrante(), guardado.getId());
        return aDTO(guardado);
    }

    @Transactional
    public CorteDTO actualizar(Long id, GenerarCorteDTO dto) {
        Corte corte = corteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Corte no encontrado con id: " + id));
        if (corte.getEstado() != EstadoCorte.PENDIENTE) {
            throw new IllegalArgumentException("Solo se puede editar un corte en estado PENDIENTE");
        }
        Turno turno = turnoRepository.findById(corte.getTurnoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado con id: " + corte.getTurnoId()));
        Dispensario dispensario = dispensarioRepository.findById(corte.getDispensarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Dispensario no encontrado con id: " + corte.getDispensarioId()));

        CalculoCorte calc = calcular(turno, dispensario, dto.getNotasCredito(), dto.getCreditos(), dto.getAceites(), dto.getObservaciones(), corte.getId());

        BigDecimal efectivoRecibido = dto.getEfectivoRecibido() != null ? dto.getEfectivoRecibido() : corte.getEfectivoRecibido();
        BigDecimal tarjetaRecibido = dto.getTarjetaRecibido() != null ? dto.getTarjetaRecibido() : corte.getTarjetaRecibido();
        BigDecimal transferenciaRecibido = dto.getTransferenciaRecibido() != null ? dto.getTransferenciaRecibido() : corte.getTransferenciaRecibido();

        BigDecimal baseCaja = calc.esperadoEfectivo()
                .add(calc.aceiteImporte())
                .subtract(calc.notasCreditoTotal())
                .subtract(calc.creditoImporteTotal());
        BigDecimal netoEfectivo = baseCaja.subtract(tarjetaRecibido).subtract(transferenciaRecibido);

        corte.setTotalVentas(calc.totalVentas());
        corte.setTotalLitros(calc.totalLitros());
        corte.setNumeroVentas(calc.numeroVentas());
        corte.setTotalAceites(calc.aceiteImporte());
        corte.setNotasCreditoTotal(calc.notasCreditoTotal());
        corte.setCreditosTotal(calc.creditoImporteTotal());
        corte.setEsperadoEfectivo(baseCaja);
        corte.setEsperadoTarjeta(calc.esperadoTarjeta());
        corte.setEsperadoTransferencia(calc.esperadoTransferencia());
        corte.setEsperadoCredito(calc.esperadoCredito());
        corte.setEfectivoRecibido(efectivoRecibido);
        corte.setTarjetaRecibido(tarjetaRecibido);
        corte.setTransferenciaRecibido(transferenciaRecibido);
        corte.setDiferenciaEfectivo(netoEfectivo.subtract(efectivoRecibido));
        corte.setObservaciones(calc.observaciones());
        corte.setNotasJson(serializar(dto.getNotasCredito()));
        corte.setCreditosJson(serializar(dto.getCreditos()));

        Corte guardado = corteRepository.save(corte);

        if (dto.getAceites() != null) {
            corteAceiteRepository.deleteAllByCorteId(guardado.getId());
            aplicarAceites(calc.surtidorAceite(), calc.filasAceite(), calc.aceitesSobrante(), guardado.getId());
        }

        return aDTO(guardado);
    }

    /**
     * El despachador del corte sale de las VENTAS del turno (quien realmente
     * vendió), no del vínculo vivo de la isla (puede ya estar liberado).
     * Si no hay ventas, se usa el de la isla como respaldo.
     */
    private Map.Entry<Long, String> despachadorDelTurno(Long turnoId, Dispensario dispensario) {
        Map<Long, String> nombres = new LinkedHashMap<>();
        Map<Long, Integer> conteo = new LinkedHashMap<>();
        for (Venta v : ventaRepository.findByTurnoIdAndDispensarioIdOrderByFechaHoraAsc(turnoId, dispensario.getId())) {
            if (v.getDespachadorId() == null) continue;
            conteo.merge(v.getDespachadorId(), 1, Integer::sum);
            nombres.putIfAbsent(v.getDespachadorId(), v.getDespachadorNombre());
        }
        Long ganador = conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(dispensario.getDespachadorId());
        return Map.entry(ganador, ganador != null ? nombres.getOrDefault(ganador, dispensario.getDespachadorNombre()) : dispensario.getDespachadorNombre());
    }

    private record CalculoCorte(
            int numeroVentas,
            BigDecimal totalVentas,
            BigDecimal totalLitros,
            BigDecimal esperadoEfectivo,
            BigDecimal esperadoTarjeta,
            BigDecimal esperadoTransferencia,
            BigDecimal esperadoCredito,
            BigDecimal aceiteImporte,
            BigDecimal notasCreditoTotal,
            BigDecimal creditoImporteTotal,
            Map<Long, BigDecimal> aceitesSobrante,
            SurtidorAceite surtidorAceite,
            List<CorteAceite> filasAceite,
            String observaciones) {
    }

    private CalculoCorte calcular(Turno turno, Dispensario dispensario,
                                  List<NotaCreditoCorteDTO> notasManual,
                                  List<CreditoCorteDTO> creditos,
                                  List<GenerarCorteAceiteDTO> aceitesDto,
                                  String observacionesBase,
                                  Long corteId) {
        List<Venta> ventas = ventaRepository.findByTurnoIdAndDispensarioIdOrderByFechaHoraAsc(turno.getId(), dispensario.getId());
        List<Venta> activas = ventas.stream()
                .filter(v -> v.getEstado() != EstadoVenta.CANCELADA)
                .toList();
        Map<String, BigDecimal> vendidoPorFamilia = new LinkedHashMap<>();
        Map<String, BigDecimal> notasPorFamilia = new LinkedHashMap<>();

        BigDecimal notasCreditoTotal = BigDecimal.ZERO;
        for (Venta v : ventas) {
            if (v.getEstado() != EstadoVenta.CANCELADA) continue;
            notasCreditoTotal = notasCreditoTotal.add(v.getTotal());
            if (v.getDetalles() != null) {
                for (var d : v.getDetalles()) {
                    if ("COMBUSTIBLE".equalsIgnoreCase(d.getTipoProducto())) {
                        String fam = familiaCombustible(d.getProductoNombre());
                        if (fam != null && d.getSubtotal() != null) {
                            notasPorFamilia.merge(fam, d.getSubtotal(), BigDecimal::add);
                        }
                        break;
                    }
                }
            }
        }

        if (notasManual != null) {
            for (NotaCreditoCorteDTO n : notasManual) {
                BigDecimal importe = n.getImporte() != null ? n.getImporte() : BigDecimal.ZERO;
                notasCreditoTotal = notasCreditoTotal.add(importe);
                String fam = n.getTipoCombustible();
                if (fam != null && !fam.isBlank()) {
                    notasPorFamilia.merge(fam.toUpperCase(), importe, BigDecimal::add);
                }
            }
        }

        try {
            List<NotaCreditoClienteDTO> notasCliente = clientesClient.listarNotasPorFechas(
                    turno.getFechaTurno(), turno.getFechaTurno());
            if (notasCliente != null) {
                for (NotaCreditoClienteDTO nc : notasCliente) {
                    if (nc.getOrigenCorte() != null) continue;
                    if (nc.getItems() == null) continue;
                    for (NotaCreditoClienteDTO.ItemNotaCreditoClienteDTO item : nc.getItems()) {
                        if (!"COMBUSTIBLE".equalsIgnoreCase(item.getTipo())) continue;
                        String fam = familiaCombustible(item.getProducto());
                        if (fam == null) continue;
                        BigDecimal importe = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;
                        if (importe.compareTo(BigDecimal.ZERO) <= 0) continue;
                        notasCreditoTotal = notasCreditoTotal.add(importe);
                        notasPorFamilia.merge(fam, importe, BigDecimal::add);
                    }
                }
            }
        } catch (Exception e) {
            // Si microservice-clientes no está disponible, continuar sin las notas
        }

        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalLitros = BigDecimal.ZERO;
        BigDecimal esperadoEfectivo = BigDecimal.ZERO;
        BigDecimal esperadoTarjeta = BigDecimal.ZERO;
        BigDecimal esperadoTransferencia = BigDecimal.ZERO;
        BigDecimal esperadoCredito = BigDecimal.ZERO;

        for (Venta v : activas) {
            totalVentas = totalVentas.add(v.getTotal());
            if (v.getDetalles() != null) {
                for (var d : v.getDetalles()) {
                    if ("COMBUSTIBLE".equalsIgnoreCase(d.getTipoProducto())) {
                        totalLitros = totalLitros.add(d.getCantidad());
                        String fam = familiaCombustible(d.getProductoNombre());
                        if (fam != null && d.getSubtotal() != null) {
                            vendidoPorFamilia.merge(fam, d.getSubtotal(), BigDecimal::add);
                        }
                    }
                }
            }
            if (v.getMetodoPago() == MetodoPago.EFECTIVO) {
                esperadoEfectivo = esperadoEfectivo.add(v.getTotal());
            } else if (v.getMetodoPago() == MetodoPago.TARJETA_CREDITO || v.getMetodoPago() == MetodoPago.TARJETA_DEBITO) {
                esperadoTarjeta = esperadoTarjeta.add(v.getTotal());
            } else if (v.getMetodoPago() == MetodoPago.TRANSFERENCIA) {
                esperadoTransferencia = esperadoTransferencia.add(v.getTotal());
            } else if (v.getMetodoPago() == MetodoPago.CREDITO) {
                esperadoCredito = esperadoCredito.add(v.getTotal());
            }
        }

        for (Map.Entry<String, BigDecimal> e : notasPorFamilia.entrySet()) {
            BigDecimal vendido = vendidoPorFamilia.getOrDefault(e.getKey(), BigDecimal.ZERO);
            if (e.getValue().compareTo(vendido) > 0) {
                throw new IllegalArgumentException("La nota de crédito de " + e.getKey() + " (" + e.getValue().setScale(2) + ") excede lo vendido de ese combustible en el corte (" + vendido.setScale(2) + ")");
            }
        }

        Map<String, BigDecimal> creditoPorFamilia = new LinkedHashMap<>();
        BigDecimal creditoImporteTotal = BigDecimal.ZERO;
        if (creditos != null) {
            for (CreditoCorteDTO c : creditos) {
                BigDecimal importe = c.getImporte() != null ? c.getImporte() : BigDecimal.ZERO;
                BigDecimal aceites = c.getAceites() != null ? c.getAceites() : BigDecimal.ZERO;
                creditoImporteTotal = creditoImporteTotal.add(importe).add(aceites);
                String fam = c.getTipoCombustible();
                if (fam != null && !fam.isBlank()) {
                    creditoPorFamilia.merge(fam.toUpperCase(), importe, BigDecimal::add);
                }
            }
        }
        for (Map.Entry<String, BigDecimal> e : creditoPorFamilia.entrySet()) {
            BigDecimal vendido = vendidoPorFamilia.getOrDefault(e.getKey(), BigDecimal.ZERO);
            if (e.getValue().compareTo(vendido) > 0) {
                throw new IllegalArgumentException("El crédito de " + e.getKey() + " ($" + e.getValue().setScale(2) + ") excede lo vendido de ese combustible en el corte ($" + vendido.setScale(2) + ")");
            }
        }

        BigDecimal aceiteImporte = BigDecimal.ZERO;
        Map<Long, BigDecimal> aceitesSobrante = new LinkedHashMap<>();
        SurtidorAceite surtidor = null;
        List<CorteAceite> filasAceite = new ArrayList<>();
        if (aceitesDto != null && !aceitesDto.isEmpty()) {
            surtidor = surtidorAceiteRepository
                    .findByDispensarioIdAndActivoTrue(dispensario.getId())
                    .orElse(null);
            if (surtidor != null) {
                List<EntregaIsla> entregas = entregaIslaRepository
                        .findBySurtidorAceiteIdAndTurnoIdOrderByFechaAsc(surtidor.getId(), turno.getId());
                List<CorteAceite> filasPrevias = corteId != null ? corteAceiteRepository.findByCorteId(corteId) : List.of();
                for (GenerarCorteAceiteDTO ac : aceitesDto) {
                    SurtidorAceiteItem item = surtidor.getItems().stream()
                            .filter(i -> Objects.equals(i.getAceiteId(), ac.getAceiteId()))
                            .findFirst().orElse(null);
                    CorteAceite filaPrevia = filasPrevias.stream()
                            .filter(f -> Objects.equals(f.getAceiteId(), ac.getAceiteId()))
                            .findFirst().orElse(null);
                    if (item == null && filaPrevia == null) continue;

                    BigDecimal recibido;
                    BigDecimal precio;
                    String nombre;
                    String categoria;
                    if (filaPrevia != null) {
                        recibido = filaPrevia.getRecibidoTotal();
                        precio = filaPrevia.getPrecioVenta();
                        nombre = filaPrevia.getAceiteNombre();
                        categoria = filaPrevia.getCategoria();
                    } else {
                        BigDecimal inicio = item.getStockInicioTurno() != null ? item.getStockInicioTurno() : BigDecimal.ZERO;
                        BigDecimal entregasTurno = entregas.stream()
                                .filter(e -> Objects.equals(e.getAceiteId(), item.getAceiteId()))
                                .map(EntregaIsla::getCantidad)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        recibido = inicio.add(entregasTurno);
                        precio = precioVigenteAceite(item.getAceiteId(), item.getPrecioVenta());
                        nombre = item.getAceiteNombre();
                        categoria = item.getCategoria();
                    }
                    BigDecimal sobrante = ac.getSobrante() != null ? ac.getSobrante() : recibido;
                    BigDecimal vendidos = recibido.subtract(sobrante).max(BigDecimal.ZERO);
                    aceiteImporte = aceiteImporte.add(vendidos.multiply(precio));
                    aceitesSobrante.put(ac.getAceiteId(), sobrante);
                    filasAceite.add(CorteAceite.builder()
                            .surtidorAceiteId(surtidor.getId())
                            .aceiteId(ac.getAceiteId())
                            .aceiteNombre(nombre)
                            .categoria(categoria)
                            .recibidoTotal(recibido)
                            .sobrante(sobrante)
                            .vendidos(vendidos)
                            .precioVenta(precio)
                            .importe(vendidos.multiply(precio))
                            .build());
                }
            }
        }

        String observaciones = limpiarObservaciones(observacionesBase);
        if (notasManual != null && !notasManual.isEmpty()) {
            StringBuilder sb = new StringBuilder(observaciones == null ? "" : observaciones);
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Notas de crédito: ");
            sb.append(notasManual.stream()
                    .map(n -> {
                        String detalle = n.getCliente() != null && !n.getCliente().isBlank() ? " " + n.getCliente() : "";
                        if (n.getLitros() != null) detalle += " " + n.getLitros() + "L";
                        return (n.getFolio() == null ? "s/n" : n.getFolio()) + detalle + " $" + (n.getImporte() != null ? n.getImporte() : BigDecimal.ZERO);
                    })
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            observaciones = sb.toString();
        }

        if (creditos != null && !creditos.isEmpty()) {
            StringBuilder sb = new StringBuilder(observaciones == null ? "" : observaciones);
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Cargos a crédito: ");
            sb.append(creditos.stream()
                    .map(c -> (c.getTipoCombustible() == null ? "" : c.getTipoCombustible() + " ")
                            + (c.getClienteNombre() == null ? "cliente?" : c.getClienteNombre())
                            + " " + c.getLitros() + "L $" + c.getImporte())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
            observaciones = sb.toString();
        }

        return new CalculoCorte(
                activas.size(),
                totalVentas,
                totalLitros,
                esperadoEfectivo,
                esperadoTarjeta,
                esperadoTransferencia,
                esperadoCredito,
                aceiteImporte,
                notasCreditoTotal,
                creditoImporteTotal,
                aceitesSobrante,
                surtidor,
                filasAceite,
                observaciones);
    }

    private String limpiarObservaciones(String observaciones) {
        if (observaciones == null) return "";
        String r = observaciones.trim();
        r = r.replaceAll("(?s)(\\s*\\|\\s*(Notas de crédito|Cargos a crédito):[^|]*)+$", "");
        r = r.replaceFirst("^(Notas de crédito|Cargos a crédito):[^|]*(\\s*\\|\\s*)?", "");
        return r.trim();
    }

    private void aplicarAceites(SurtidorAceite surtidor, List<CorteAceite> filas, Map<Long, BigDecimal> aceitesSobrante, Long corteId) {
        if (filas == null || filas.isEmpty()) return;
        for (CorteAceite f : filas) {
            CorteAceite fila = CorteAceite.builder()
                    .corteId(corteId)
                    .surtidorAceiteId(f.getSurtidorAceiteId())
                    .aceiteId(f.getAceiteId())
                    .aceiteNombre(f.getAceiteNombre())
                    .categoria(f.getCategoria())
                    .recibidoTotal(f.getRecibidoTotal())
                    .sobrante(f.getSobrante())
                    .vendidos(f.getVendidos())
                    .precioVenta(f.getPrecioVenta())
                    .importe(f.getImporte())
                    .build();
            corteAceiteRepository.save(fila);
        }
        if (surtidor != null) {
            for (SurtidorAceiteItem item : surtidor.getItems()) {
                BigDecimal sobrante = aceitesSobrante != null ? aceitesSobrante.get(item.getAceiteId()) : null;
                if (sobrante == null) continue;
                item.setStockActual(sobrante);
                item.setStockInicioTurno(sobrante);
            }
            surtidorAceiteRepository.save(surtidor);
        }
    }

    @Transactional
    public CorteDTO validar(Long id, String validadoPor) {
        Corte corte = corteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Corte no encontrado con id: " + id));
        if (corte.getEstado() != EstadoCorte.PENDIENTE) {
            throw new IllegalArgumentException("El corte " + corte.getCodigoCorte() + " ya fue procesado");
        }

        generarNotasCreditoClientes(corte);

        corte.setEstado(EstadoCorte.VALIDADO);
        corte.setValidadoPor(validadoPor);
        corte.setValidadoFecha(LocalDateTime.now());

        if (corte.getDiferenciaEfectivo().compareTo(BigDecimal.ZERO) > 0) {
            registrarIncidenciaCorte(corte, validadoPor, "FALTANTE",
                    "Faltante de efectivo en corte " + corte.getCodigoCorte()
                            + " del dispensario " + corte.getDispensarioNombre());
        } else if (corte.getDiferenciaEfectivo().compareTo(BigDecimal.ZERO) < 0) {
            registrarIncidenciaCorte(corte, validadoPor, "SOBRANTE",
                    "Sobrante de efectivo en corte " + corte.getCodigoCorte()
                            + " del dispensario " + corte.getDispensarioNombre());
        }
        return aDTO(corteRepository.save(corte));
    }

    private void registrarIncidenciaCorte(Corte corte, String validadoPor, String tipo, String observaciones) {
        if (corte.getDespachadorId() == null) {
            throw new IllegalArgumentException("El corte " + corte.getCodigoCorte()
                    + " no tiene despachador asignado: no se puede registrar la incidencia de caja en Nómina");
        }
        nominaClient.registrarIncidencia(RegistrarIncidenciaDTO.builder()
                .empleadoId(corte.getDespachadorId())
                .tipo(tipo)
                .fecha(LocalDate.now())
                .monto(corte.getDiferenciaEfectivo().abs())
                .observaciones(observaciones)
                .autorizadoPor(validadoPor)
                .build());
    }

    private void generarNotasCreditoClientes(Corte corte) {
        List<CreditoCorteDTO> creditos = deserializar(corte.getCreditosJson(), new TypeReference<List<CreditoCorteDTO>>() {});
        if (creditos == null || creditos.isEmpty()) return;

        List<CreditoClienteDTO> creditosActivos = new ArrayList<>();
        try {
            List<CreditoClienteDTO> creditosConSaldo = clientesClient.listarCreditosActivosConSaldo();
            if (creditosConSaldo != null) creditosActivos.addAll(creditosConSaldo);
        } catch (Exception e) {
            // microservice-clientes no disponible; resolverCréditoId retornará null para cargos sin creditoId
        }

        LocalDate fechaMinima = creditos.stream()
                .map(CreditoCorteDTO::getFecha)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate fechaMaxima = creditos.stream()
                .map(CreditoCorteDTO::getFecha)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        Set<String> yaGeneradas = new HashSet<>();
        try {
            List<NotaCreditoClienteDTO> existentes = clientesClient.listarNotasPorFechas(fechaMinima, fechaMaxima);
            if (existentes != null) {
                for (NotaCreditoClienteDTO nc : existentes) {
                    if (nc.getOrigenCorte() != null) yaGeneradas.add(nc.getOrigenCorte());
                }
            }
        } catch (Exception e) {
            // Si microservice-clientes no está disponible, continuar para reintentar al crear
        }

        boolean error = false;
        List<String> mensajes = new ArrayList<>();
        for (int i = 0; i < creditos.size(); i++) {
            CreditoCorteDTO cargo = creditos.get(i);
            Long creditoId = resolverCreditoId(cargo, creditosActivos);
            if (creditoId == null) {
                error = true;
                mensajes.add("No se pudo determinar el crédito de "
                        + (cargo.getClienteNombre() != null ? cargo.getClienteNombre() : "cliente id " + cargo.getClienteId()));
                continue;
            }
            String origen = originPorCargo(corte, i);
            if (yaGeneradas.contains(origen) || yaGeneradas.contains(corte.getCodigoCorte() + ":" + creditoId)) continue;
            try {
                clientesClient.crearNotaCredito(notaCreditoDesdeCargo(cargo, corte, creditoId, origen));
            } catch (Exception e) {
                error = true;
                String causa = e.getMessage();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    causa = e.getCause().getMessage();
                }
                mensajes.add("Cargo de " + (cargo.getClienteNombre() != null ? cargo.getClienteNombre() : "cliente")
                        + ": " + causa);
            }
        }
        if (error) {
            throw new IllegalStateException("No se pudieron generar las notas de crédito en clientes. "
                    + String.join(" | ", mensajes));
        }
    }

    private Long resolverCreditoId(CreditoCorteDTO cargo, List<CreditoClienteDTO> creditosActivos) {
        if (cargo.getCreditoId() != null) return cargo.getCreditoId();
        if (cargo.getClienteId() == null || creditosActivos.isEmpty()) return null;
        return creditosActivos.stream()
                .filter(c -> Objects.equals(c.getClienteId(), cargo.getClienteId()))
                .filter(c -> c.getSaldoPendiente() != null && c.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
                .max(java.util.Comparator.comparing(CreditoClienteDTO::getSaldoPendiente))
                .map(CreditoClienteDTO::getId)
                .orElse(null);
    }

    private String originPorCargo(Corte corte, int indice) {
        return corte.getCodigoCorte() + ":cargo:" + indice;
    }

    private CrearNotaCreditoRequestDTO notaCreditoDesdeCargo(CreditoCorteDTO cargo, Corte corte,
                                                             Long creditoId, String origen) {
        BigDecimal importe = cargo.getImporte() != null ? cargo.getImporte() : BigDecimal.ZERO;
        BigDecimal litros = cargo.getLitros() != null ? cargo.getLitros() : BigDecimal.ZERO;
        BigDecimal aceites = cargo.getAceites() != null ? cargo.getAceites() : BigDecimal.ZERO;

        List<CrearNotaCreditoRequestDTO.Item> items = new ArrayList<>();
        if (importe.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal precioUnitario = litros.compareTo(BigDecimal.ZERO) > 0
                    ? importe.divide(litros, 2, java.math.RoundingMode.HALF_UP)
                    : importe;
            items.add(CrearNotaCreditoRequestDTO.Item.builder()
                    .tipo("COMBUSTIBLE")
                    .producto(cargo.getTipoCombustible() != null ? cargo.getTipoCombustible() : "COMBUSTIBLE")
                    .cantidad(litros.compareTo(BigDecimal.ZERO) > 0 ? litros : BigDecimal.ONE)
                    .unidad("L")
                    .precioUnitario(precioUnitario)
                    .subtotal(importe)
                    .build());
        }
        if (aceites.compareTo(BigDecimal.ZERO) > 0) {
            items.add(CrearNotaCreditoRequestDTO.Item.builder()
                    .tipo("ACEITE")
                    .producto("ACEITES")
                    .cantidad(BigDecimal.ONE)
                    .unidad("UN")
                    .precioUnitario(aceites)
                    .subtotal(aceites)
                    .build());
        }

        return CrearNotaCreditoRequestDTO.builder()
                .creditoId(creditoId)
                .fechaCarga(cargo.getFecha() != null ? cargo.getFecha() : LocalDate.now())
                .origenCorte(origen)
                .items(items)
                .build();
    }

    @Transactional
    public CorteDTO cerrar(Long id) {
        Corte corte = corteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Corte no encontrado con id: " + id));
        if (corte.getEstado() != EstadoCorte.VALIDADO) {
            throw new IllegalArgumentException("El corte debe estar VALIDADO antes de cerrarse");
        }
        corte.setEstado(EstadoCorte.CERRADO);
        return aDTO(corteRepository.save(corte));
    }

    @Transactional
    public Map<String, Object> reprocesarNotasCreditoClientes() {
        List<Corte> cortes = corteRepository.findAll();
        List<String> procesados = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        for (Corte corte : cortes) {
            if (corte.getEstado() != EstadoCorte.VALIDADO && corte.getEstado() != EstadoCorte.CERRADO) {
                continue;
            }
            try {
                generarNotasCreditoClientes(corte);
                procesados.add(corte.getCodigoCorte());
            } catch (Exception e) {
                errores.add(corte.getCodigoCorte() + ": " + e.getMessage());
            }
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("procesados", procesados);
        resultado.put("errores", errores);
        return resultado;
    }

    private String generarCodigo() {
        Long numero = corteRepository.findFirstByOrderByIdDesc()
                .map(c -> Long.parseLong(c.getCodigoCorte().substring(6)))
                .orElse(0L) + 1;
        return String.format("CORTE-%05d", numero);
    }

    private String familiaCombustible(String nombre) {
        if (nombre == null) return null;
        String n = nombre.toUpperCase();
        if (n.contains("MAGNA")) return "MAGNA";
        if (n.contains("PREMIUM")) return "PREMIUM";
        if (n.contains("DIESEL") || n.contains("DIÉSEL")) return "DIESEL";
        return null;
    }
}