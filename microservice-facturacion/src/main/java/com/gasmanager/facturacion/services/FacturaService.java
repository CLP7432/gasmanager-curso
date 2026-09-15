package com.gasmanager.facturacion.services;

import com.gasmanager.facturacion.clients.ClientesClient;
import com.gasmanager.facturacion.clients.VentasClient;
import com.gasmanager.facturacion.config.FacturacionProperties;
import com.gasmanager.facturacion.dto.*;
import com.gasmanager.facturacion.entities.ClienteFiscal;
import com.gasmanager.facturacion.entities.Factura;
import com.gasmanager.facturacion.entities.FacturaConcepto;
import com.gasmanager.facturacion.enums.EstadoFactura;
import com.gasmanager.facturacion.enums.OrigenConceptoFactura;
import com.gasmanager.facturacion.exceptions.RecursoNoEncontradoException;
import com.gasmanager.facturacion.repositories.ClienteFiscalRepository;
import com.gasmanager.facturacion.repositories.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FacturaService {

    private static final String ESTADO_CANCELADA_EN_VENTAS = "CANCELADA";

    private final FacturaRepository facturaRepository;
    private final ClienteFiscalRepository clienteFiscalRepository;
    private final ClientesClient clientesClient;
    private final VentasClient ventasClient;
    private final FacturacionProperties props;
    private final CfdiGeneratorService cfdiGenerator;
    private final PdfService pdfService;
    private final CorreoService correoService;

    @Transactional(readOnly = true)
    public DisponiblesResponseDTO disponibles(Long clienteId) {
        List<NotaCreditoDTO> notas = new ArrayList<>();
        try {
            notas = clientesClient.listarNotasPorCliente(clienteId);
        } catch (Exception e) {
            log.warn("No se pudieron cargar las notas del cliente {}: {}", clienteId, e.getMessage());
        }

        // REGLA: solo se facturan notas LIQUIDADAS (PAGADA). Las ACTIVA
        // primero se pagan en el módulo de clientes.
        List<NotaCreditoDTO> validas = notas.stream()
                .filter(n -> "PAGADA".equals(n.getEstado()))
                .toList();

        List<ItemDisponibleDTO> items = validas.stream()
                .flatMap(n -> {
                    List<NotaCreditoDTO.ItemNotaCreditoDTO> lineas = n.getItems() != null
                            ? n.getItems()
                            : List.of(NotaCreditoDTO.ItemNotaCreditoDTO.builder()
                                    .tipo("COMBUSTIBLE")
                                    .producto("Nota " + n.getNumero())
                                    .cantidad(BigDecimal.ONE)
                                    .unidad("Pieza")
                                    .precioUnitario(n.getSaldo())
                                    .build());
                    return lineas.stream().map(it -> fromItem(n, it));
                })
                .toList();

        String nombre = validas.stream().map(NotaCreditoDTO::getClienteNombre)
                .filter(Objects::nonNull).findFirst().orElse(null);

        return DisponiblesResponseDTO.builder()
                .clienteId(clienteId)
                .clienteNombre(nombre)
                .notas(items)
                .build();
    }

    private ItemDisponibleDTO fromItem(NotaCreditoDTO nota, NotaCreditoDTO.ItemNotaCreditoDTO it) {
        boolean combustible = it.getTipo() != null
                && (it.getTipo().toUpperCase().contains("COMB") || it.getTipo().toUpperCase().contains("GAS")
                    || it.getTipo().toUpperCase().contains("DIESEL"));
        String producto = it.getProducto() == null ? "Nota " + nota.getNumero() : it.getProducto();
        String clave = combustible ? claveCombustible(producto) : props.getCfdi().getClaves().getClaveProdServAceite();
        String claveUnidad = combustible
                ? props.getCfdi().getClaves().getClaveUnidadLitro()
                : props.getCfdi().getClaves().getClaveUnidadPieza();
        String unidad = it.getUnidad() != null ? it.getUnidad() : (combustible ? "L" : "Pieza");
        return ItemDisponibleDTO.builder()
                .origenId(nota.getId())
                .origenFolio(nota.getNumero())
                .descripcion(producto)
                .cantidad(it.getCantidad() != null ? it.getCantidad() : BigDecimal.ONE)
                .unidadClave(claveUnidad)
                .unidad(unidad)
                .valorUnitario(it.getPrecioUnitario() != null
                        ? it.getPrecioUnitario().setScale(4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                .esCombustible(combustible)
                .tipoCombustible(combustible ? "03" : null)
                .yaFacturada(yaFacturado(OrigenConceptoFactura.NOTA_CREDITO, nota.getNumero()))
                .build();
    }

    private String claveCombustible(String producto) {
        String p = producto.toUpperCase();
        if (p.contains("PREMIUM")) return props.getCfdi().getClaves().getClaveProdServPremium();
        if (p.contains("DIESEL") || p.contains("DIÉSEL")) return props.getCfdi().getClaves().getClaveProdServDiesel();
        return props.getCfdi().getClaves().getClaveProdServRegular();
    }

    @Transactional
    public FacturaResponseDTO facturar(CrearFacturaRequestDTO request) {
        ClienteFiscal clientefiscal = clienteFiscalRepository.findById(request.getClienteFiscalId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente fiscal no encontrado con id: " + request.getClienteFiscalId()));

        if (request.getConceptos() == null || request.getConceptos().isEmpty()) {
            throw new IllegalArgumentException("La factura debe incluir al menos un concepto");
        }

        validarDuplicados(request.getConceptos());
        validarOrigenes(clientefiscal, request.getConceptos());

        LocalDateTime fecha = LocalDateTime.now();
        String serie = props.getCfdi().getSerie();
        String numero = siguienteNumero();
        String folio = serie + "-" + numero;
        String uuid = UUID.randomUUID().toString().toUpperCase();

        Factura factura = Factura.builder()
                .folio(folio)
                .uuid(uuid)
                .serie(serie)
                .fechaEmision(fecha)
                .clienteFiscal(clientefiscal)
                .receptorRfc(clientefiscal.getRfc())
                .receptorNombre(clientefiscal.getRazonSocial())
                .receptorRegimenFiscal(clientefiscal.getRegimenFiscal())
                .receptorCodigoPostal(clientefiscal.getCodigoPostal())
                .receptorUsoCfdi(clientefiscal.getUsoCfdi() != null ? clientefiscal.getUsoCfdi() : props.getCfdi().getUsoCfdiPorDefecto())
                .formaPago(request.getFormaPago() != null ? request.getFormaPago() : props.getCfdi().getFormaPagoPorDefecto())
                .metodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : props.getCfdi().getMetodoPagoPorDefecto())
                .build();

        for (ConceptoFacturaRequestDTO c : request.getConceptos()) {
            factura.getConceptos().add(concepto(factura, c));
        }

        anclarTickets(factura, request.getConceptos());

        recalcTotales(factura);

        if (props.getCfdi().isSimulacion()) {
            factura.setEstado(EstadoFactura.SIMULADA);
            factura.setTimbrada(false);
        } else {
            factura.setEstado(EstadoFactura.TIMBRADA);
            factura.setTimbrada(true);
            factura.setFechaTimbrado(fecha);
        }

        facturaRepository.save(factura);
        generarDocumentos(factura);

        log.info("Factura generada: {} total={} simulada={}", factura.getFolio(), factura.getTotal(), props.getCfdi().isSimulacion());
        return aDTO(facturaRepository.save(factura));
    }

    /**
     * Los tickets cierran su total en bomba (ej. VEN-00002 = $500.00) con litros
     * redondeados a 2 decimales, así que sus renglones no siempre suman el total.
     * Para que la factura dé exacta se reparten el subtotal e IVA del ticket
     * entre sus conceptos a prorrata del bruto; el último absorbe el redondeo.
     */
    private void anclarTickets(Factura factura, List<ConceptoFacturaRequestDTO> req) {
        Map<String, List<Integer>> porFolio = new LinkedHashMap<>();
        for (int i = 0; i < req.size(); i++) {
            if (req.get(i).getOrigen() == OrigenConceptoFactura.TICKET
                    && req.get(i).getOrigenFolio() != null && !req.get(i).getOrigenFolio().isBlank()) {
                porFolio.computeIfAbsent(req.get(i).getOrigenFolio(), k -> new ArrayList<>()).add(i);
            }
        }
        for (Map.Entry<String, List<Integer>> e : porFolio.entrySet()) {
            try {
                VentaDTO venta = ventasClient.obtenerVentaPorFolio(e.getKey());
                if (venta == null || venta.getSubtotal() == null || venta.getIva() == null) continue;
                List<Integer> idx = e.getValue();
                List<BigDecimal> brutos = idx.stream()
                        .map(i -> req.get(i).getCantidad().multiply(req.get(i).getValorUnitario()))
                        .toList();
                BigDecimal totalBruto = brutos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalBruto.compareTo(BigDecimal.ZERO) <= 0) continue;
                BigDecimal impAcum = BigDecimal.ZERO;
                BigDecimal ivaAcum = BigDecimal.ZERO;
                for (int j = 0; j < idx.size(); j++) {
                    FacturaConcepto fc = factura.getConceptos().get(idx.get(j));
                    BigDecimal parte = brutos.get(j).divide(totalBruto, 10, RoundingMode.HALF_UP);
                    BigDecimal imp;
                    BigDecimal iva;
                    if (j < idx.size() - 1) {
                        imp = venta.getSubtotal().multiply(parte).setScale(2, RoundingMode.HALF_UP);
                        iva = venta.getIva().multiply(parte).setScale(2, RoundingMode.HALF_UP);
                        impAcum = impAcum.add(imp);
                        ivaAcum = ivaAcum.add(iva);
                    } else {
                        imp = venta.getSubtotal().subtract(impAcum);
                        iva = venta.getIva().subtract(ivaAcum);
                    }
                    fc.setImporte(imp);
                    fc.setIva(iva);
                    fc.setValorUnitario(imp.divide(fc.getCantidad(), 4, RoundingMode.HALF_UP));
                }
            } catch (Exception ex) {
                log.warn("No se pudo anclar el ticket {}: {}", e.getKey(), ex.getMessage());
            }
        }
    }

    private void validarDuplicados(List<ConceptoFacturaRequestDTO> conceptos) {
        Set<String> vistos = new HashSet<>();
        for (ConceptoFacturaRequestDTO c : conceptos) {
            if (c.getOrigenFolio() == null || c.getOrigenFolio().isBlank()) continue;
            // La llave incluye el renglón: una nota con combustible + aceite
            // genera un concepto por consumo y NO es documento repetido.
            // El guard yaFacturado (por origen+folio en BD) sigue evitando
            // facturar la misma nota en dos facturas distintas.
            String clave = c.getOrigen() + "::" + c.getOrigenFolio()
                    + "::" + c.getDescripcion() + "::" + c.getCantidad() + "::" + c.getValorUnitario();
            if (!vistos.add(clave)) {
                throw new IllegalArgumentException("El concepto " + c.getDescripcion() + " del documento " + c.getOrigenFolio() + " está repetido en la factura");
            }
            if (yaFacturado(c.getOrigen(), c.getOrigenFolio())) {
                throw new IllegalArgumentException("El documento " + c.getOrigenFolio() + " ya fue facturado");
            }
        }
    }

    private void validarOrigenes(ClienteFiscal clienteFiscal, List<ConceptoFacturaRequestDTO> conceptos) {
        for (ConceptoFacturaRequestDTO c : conceptos) {
            if (c.getOrigen() == OrigenConceptoFactura.NOTA_CREDITO) {
                if (c.getOrigenFolio() == null || c.getOrigenFolio().isBlank()) {
                    throw new IllegalArgumentException("El folio de la nota de crédito es obligatorio");
                }
                List<NotaCreditoDTO> notas = clientesClient.listarNotasPorCliente(clienteFiscal.getClienteId());
                NotaCreditoDTO nota = notas.stream()
                        .filter(n -> c.getOrigenFolio().equals(n.getNumero()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "La nota de crédito " + c.getOrigenFolio() + " no pertenece al cliente"));
                if (!"PAGADA".equals(nota.getEstado())) {
                    throw new IllegalArgumentException("La nota de crédito " + c.getOrigenFolio()
                            + " aún no está liquidada (estado: " + nota.getEstado() + "). Liqúidala primero en Clientes.");
                }
            }
            if (c.getOrigen() == OrigenConceptoFactura.TICKET) {
                if (c.getOrigenFolio() == null || c.getOrigenFolio().isBlank()) {
                    throw new IllegalArgumentException("El folio del ticket es obligatorio");
                }
                VentaDTO venta = ventasClient.obtenerVentaPorFolio(c.getOrigenFolio());
                if (venta != null && ESTADO_CANCELADA_EN_VENTAS.equals(venta.getEstado())) {
                    throw new IllegalArgumentException("El ticket " + c.getOrigenFolio() + " está cancelado y no puede facturarse");
                }
            }
        }
    }

    private FacturaConcepto concepto(Factura factura, ConceptoFacturaRequestDTO c) {
        // El valor unitario llega NETO (sin IVA, como en el concepto impreso).
        // Los renglones TICKET se anclan después a los totales del ticket.
        BigDecimal importe = c.getCantidad().multiply(c.getValorUnitario()).setScale(2, RoundingMode.HALF_UP);
        boolean combustible = Boolean.TRUE.equals(c.getEsCombustible());
        BigDecimal iva = importe.multiply(props.getCfdi().getIvaTasa()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ieps = combustible
                ? importe.multiply(props.getCfdi().getIepsTasa()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return FacturaConcepto.builder()
                .factura(factura)
                .origen(c.getOrigen())
                .origenId(c.getOrigenId())
                .origenFolio(c.getOrigenFolio())
                .descripcion(c.getDescripcion())
                .claveProdServ(c.getClaveProdServ())
                .claveUnidad(c.getClaveUnidad())
                .unidad(c.getUnidad())
                .cantidad(c.getCantidad())
                .valorUnitario(c.getValorUnitario().setScale(4, RoundingMode.HALF_UP))
                .importe(importe)
                .iva(iva)
                .ieps(ieps)
                .esCombustible(combustible)
                .tipoCombustible(c.getTipoCombustible())
                .build();
    }

    private void recalcTotales(Factura factura) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;
        BigDecimal ieps = BigDecimal.ZERO;
        for (FacturaConcepto c : factura.getConceptos()) {
            subtotal = subtotal.add(c.getImporte());
            iva = iva.add(c.getIva());
            ieps = ieps.add(c.getIeps());
        }
        factura.setSubtotal(subtotal);
        factura.setIva(iva);
        factura.setIeps(ieps);
        factura.setTotal(subtotal.add(iva).add(ieps));
    }

    private void generarDocumentos(Factura factura) {
        try {
            String xml = cfdiGenerator.generarXml(factura);
            byte[] pdf = pdfService.generarPdf(factura);

            Path dir = Paths.get(props.getCfdi().getDocumentos().getRuta());
            Files.createDirectories(dir);

            Path xmlPath = dir.resolve(factura.getUuid() + ".xml");
            Path pdfPath = dir.resolve(factura.getUuid() + ".pdf");
            Files.write(xmlPath, xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Files.write(pdfPath, pdf);

            factura.setXmlPath(xmlPath.toAbsolutePath().toString());
            factura.setPdfPath(pdfPath.toAbsolutePath().toString());
        } catch (Exception e) {
            log.error("No se pudieron generar los documentos de la factura {}", factura.getFolio(), e);
            throw new IllegalArgumentException("No se pudieron generar XML/PDF: " + e.getMessage());
        }
    }

    private String siguienteNumero() {
        long cantidad = facturaRepository.countByFechaEmisionBetween(
                LocalDate.now().atStartOfDay(), LocalDate.now().atTime(23, 59, 59));
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return fecha + "-" + String.format("%04d", cantidad + 1);
    }

    private boolean yaFacturado(OrigenConceptoFactura origen, String folio) {
        return facturaRepository.countConceptosYaFacturados(origen, folio, EstadoFactura.CANCELADA) > 0;
    }

    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listar() {
        return facturaRepository.findAllByOrderByFechaEmisionDesc().stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> listarPorCliente(Long clienteFiscalId) {
        return facturaRepository.findByClienteFiscalIdOrderByFechaEmisionDesc(clienteFiscalId)
                .stream().map(this::aDTO).toList();
    }

    @Transactional(readOnly = true)
    public FacturaResponseDTO obtener(Long id) {
        return aDTO(buscar(id));
    }

    @Transactional
    public FacturaResponseDTO cancelar(Long id, String motivo) {
        Factura factura = buscar(id);
        if (factura.getEstado() == EstadoFactura.CANCELADA) {
            throw new IllegalStateException("La factura ya está cancelada");
        }
        factura.setEstado(EstadoFactura.CANCELADA);
        factura.setMotivoCancelacion(motivo);
        return aDTO(facturaRepository.save(factura));
    }

    public byte[] xml(Long id) {
        Factura factura = buscar(id);
        return leerArchivo(factura.getXmlPath(), "XML");
    }

    public byte[] pdf(Long id) {
        Factura factura = buscar(id);
        return leerArchivo(factura.getPdfPath(), "PDF");
    }

    @Transactional
    public boolean enviarCorreo(Long id) {
        Factura factura = buscar(id);
        if (factura.isCorreoEnviado()) {
            throw new IllegalStateException("El correo de la factura " + factura.getFolio() + " ya fue enviado");
        }
        byte[] pdf = leerArchivo(factura.getPdfPath(), "PDF");
        byte[] xml = leerArchivo(factura.getXmlPath(), "XML");
        String destino = factura.getClienteFiscal().getCorreo();

        if (destino == null || destino.isBlank()) {
            throw new IllegalStateException("El cliente fiscal no tiene correo registrado");
        }

        boolean enviado = correoService.enviar(destino, "Factura " + factura.getFolio(),
                "Adjuntamos la factura " + factura.getFolio() + " (CFDI 4.0).", pdf, xml);
        if (enviado) {
            factura.setCorreoEnviado(true);
            facturaRepository.save(factura);
        }
        return enviado;
    }

    private byte[] leerArchivo(String ruta, String tipo) {
        if (ruta == null) return new byte[0];
        try {
            return Files.readAllBytes(Paths.get(ruta));
        } catch (Exception e) {
            throw new RecursoNoEncontradoException("No se encontró el archivo " + tipo + " de la factura");
        }
    }

    private Factura buscar(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Factura no encontrada con id: " + id));
    }

    public FacturaResponseDTO aDTO(Factura f) {
        return FacturaResponseDTO.builder()
                .id(f.getId())
                .folio(f.getFolio())
                .uuid(f.getUuid())
                .serie(f.getSerie())
                .fechaEmision(f.getFechaEmision())
                .clienteFiscalId(f.getClienteFiscal().getId())
                .clienteId(f.getClienteFiscal().getClienteId())
                .receptorRfc(f.getReceptorRfc())
                .receptorNombre(f.getReceptorNombre())
                .receptorRegimenFiscal(f.getReceptorRegimenFiscal())
                .receptorCodigoPostal(f.getReceptorCodigoPostal())
                .receptorUsoCfdi(f.getReceptorUsoCfdi())
                .subtotal(f.getSubtotal())
                .iva(f.getIva())
                .ieps(f.getIeps())
                .total(f.getTotal())
                .formaPago(f.getFormaPago())
                .metodoPago(f.getMetodoPago())
                .estado(f.getEstado())
                .timbrada(f.isTimbrada())
                .fechaTimbrado(f.getFechaTimbrado())
                .correoEnviado(f.isCorreoEnviado())
                .conceptos(f.getConceptos().stream().map(this::aConceptoDTO).toList())
                .build();
    }

    private FacturaConceptoDTO aConceptoDTO(FacturaConcepto c) {
        return FacturaConceptoDTO.builder()
                .id(c.getId())
                .origen(c.getOrigen())
                .origenId(c.getOrigenId())
                .origenFolio(c.getOrigenFolio())
                .descripcion(c.getDescripcion())
                .claveProdServ(c.getClaveProdServ())
                .claveUnidad(c.getClaveUnidad())
                .unidad(c.getUnidad())
                .cantidad(c.getCantidad())
                .valorUnitario(c.getValorUnitario())
                .importe(c.getImporte())
                .iva(c.getIva())
                .ieps(c.getIeps())
                .esCombustible(c.isEsCombustible())
                .tipoCombustible(c.getTipoCombustible())
                .build();
    }
}