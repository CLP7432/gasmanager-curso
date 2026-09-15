package com.gasmanager.clientes.services;

import com.gasmanager.clientes.dto.ItemNotaCreditoDTO;
import com.gasmanager.clientes.dto.LiquidarNotasRequestDTO;
import com.gasmanager.clientes.dto.NotaCreditoDTO;
import com.gasmanager.clientes.entities.AbonoCredito;
import com.gasmanager.clientes.entities.Credito;
import com.gasmanager.clientes.entities.ItemNotaCredito;
import com.gasmanager.clientes.entities.NotaCredito;
import com.gasmanager.clientes.enums.EstadoCredito;
import com.gasmanager.clientes.enums.EstadoNotaCredito;
import com.gasmanager.clientes.exceptions.RecursoNoEncontradoException;
import com.gasmanager.clientes.repositories.AbonoCreditoRepository;
import com.gasmanager.clientes.repositories.CreditoRepository;
import com.gasmanager.clientes.repositories.ItemNotaCreditoRepository;
import com.gasmanager.clientes.repositories.NotaCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotaCreditoService {

    private final NotaCreditoRepository notaRepository;
    private final ItemNotaCreditoRepository itemRepository;
    private final CreditoRepository creditoRepository;
    private final AbonoCreditoRepository abonoRepository;

    public NotaCreditoDTO aDTO(NotaCredito n) {
        return NotaCreditoDTO.builder()
                .id(n.getId())
                .numero(n.getNumero())
                .creditoId(n.getCredito().getId())
                .creditoFolio(n.getCredito().getFolioCredito())
                .clienteNombre(nombreClienteDe(n.getCredito()))
                .clienteId(n.getCredito().getCliente() != null ? n.getCredito().getCliente().getId() : null)
                .saldo(n.getSaldo())
                .vehiculo(n.getVehiculo())
                .conductor(n.getConductor())
                .fechaCarga(n.getFechaCarga())
                .estado(n.getEstado())
                .origenCorte(n.getOrigenCorte())
                .items(n.getItems() == null ? List.of() :
                        n.getItems().stream().map(this::aItemDTO).toList())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }

    public ItemNotaCreditoDTO aItemDTO(ItemNotaCredito i) {
        return ItemNotaCreditoDTO.builder()
                .id(i.getId())
                .tipo(i.getTipo())
                .producto(i.getProducto())
                .cantidad(i.getCantidad())
                .unidad(i.getUnidad())
                .precioUnitario(i.getPrecioUnitario())
                .subtotal(i.getSubtotal())
                .build();
    }

    public List<NotaCreditoDTO> listar() {
        return notaRepository.findAll().stream()
                .filter(n -> n.getCredito().getEstado() != EstadoCredito.PAGADO
                        && n.getCredito().getEstado() != EstadoCredito.CANCELADO)
                .map(this::aDTO)
                .collect(Collectors.toList());
    }

    public List<NotaCreditoDTO> listarPorCredito(Long creditoId) {
        return notaRepository.findByCreditoIdOrderByCreatedAtDesc(creditoId)
                .stream().map(this::aDTO).collect(Collectors.toList());
    }

    public List<NotaCreditoDTO> listarPorCliente(Long clienteId) {
        return notaRepository.findByCredito_Cliente_IdOrderByCreatedAtDesc(clienteId)
                .stream().map(this::aDTO).collect(Collectors.toList());
    }

    public List<NotaCreditoDTO> listarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        return notaRepository.findByFechaCargaBetween(desde, hasta)
                .stream().map(this::aDTO).collect(Collectors.toList());
    }

    public NotaCreditoDTO obtenerPorId(Long id) {
        NotaCredito n = notaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota de crédito no encontrada: " + id));
        return aDTO(n);
    }

    public NotaCreditoDTO crear(NotaCreditoDTO dto) {
        Credito credito = creditoRepository.findById(dto.getCreditoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Crédito no encontrado: " + dto.getCreditoId()));

        validarCreditoDisponible(credito);

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalStateException("La nota debe registrar al menos un producto cargado");
        }

        BigDecimal totalItems = dto.getItems().stream()
                .map(this::subtotalDe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (credito.getSaldoPendiente().compareTo(totalItems) < 0) {
            throw new IllegalStateException("El crédito no tiene saldo suficiente. Disponible: " + credito.getSaldoPendiente());
        }

        NotaCredito nota = NotaCredito.builder()
                .numero(generarNumero())
                .credito(credito)
                .saldo(totalItems)
                .vehiculo(dto.getVehiculo())
                .conductor(dto.getConductor())
                .fechaCarga(dto.getFechaCarga() != null ? dto.getFechaCarga() : LocalDate.now())
                .origenCorte(dto.getOrigenCorte())
                .estado(EstadoNotaCredito.ACTIVA)
                .build();

        for (ItemNotaCreditoDTO itemDto : dto.getItems()) {
            ItemNotaCredito item = ItemNotaCredito.builder()
                    .tipo(itemDto.getTipo())
                    .producto(itemDto.getProducto())
                    .cantidad(itemDto.getCantidad())
                    .unidad(itemDto.getUnidad())
                    .precioUnitario(itemDto.getPrecioUnitario())
                    .subtotal(itemDto.getSubtotal())
                    .build();
            item.calcularSubtotal();
            item.setNotaCredito(nota);
            nota.getItems().add(item);
        }

        credito.setSaldoPendiente(credito.getSaldoPendiente().subtract(totalItems));
        creditoRepository.save(credito);

        return aDTO(notaRepository.save(nota));
    }

    public NotaCreditoDTO actualizar(Long id, NotaCreditoDTO dto) {
        NotaCredito nota = notaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada: " + id));

        if (nota.getEstado() == EstadoNotaCredito.AGOTADA
                || nota.getEstado() == EstadoNotaCredito.BLOQUEADA
                || nota.getEstado() == EstadoNotaCredito.PAGADA) {
            throw new IllegalStateException("No se puede editar una nota " + nota.getEstado().name().toLowerCase());
        }

        validarCreditoDisponible(nota.getCredito());

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalStateException("La nota debe registrar al menos un producto cargado");
        }

        BigDecimal totalNuevo = dto.getItems().stream()
                .map(this::subtotalDe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Credito credito = nota.getCredito();
        BigDecimal totalViejo = nota.getSaldo();
        BigDecimal delta = totalNuevo.subtract(totalViejo);

        if (delta.compareTo(BigDecimal.ZERO) > 0
                && credito.getSaldoPendiente().compareTo(delta) < 0) {
            throw new IllegalStateException("El crédito no tiene saldo suficiente para el nuevo total. Disponible: " + credito.getSaldoPendiente());
        }

        nota.getItems().clear();
        for (ItemNotaCreditoDTO itemDto : dto.getItems()) {
            ItemNotaCredito item = ItemNotaCredito.builder()
                    .tipo(itemDto.getTipo())
                    .producto(itemDto.getProducto())
                    .cantidad(itemDto.getCantidad())
                    .unidad(itemDto.getUnidad())
                    .precioUnitario(itemDto.getPrecioUnitario())
                    .subtotal(itemDto.getSubtotal())
                    .build();
            item.calcularSubtotal();
            item.setNotaCredito(nota);
            nota.getItems().add(item);
        }
        nota.setSaldo(totalNuevo);
        nota.setVehiculo(dto.getVehiculo());
        nota.setConductor(dto.getConductor());
        if (dto.getFechaCarga() != null) {
            nota.setFechaCarga(dto.getFechaCarga());
        }

        credito.setSaldoPendiente(credito.getSaldoPendiente().add(totalViejo).subtract(totalNuevo));
        creditoRepository.save(credito);

        return aDTO(notaRepository.save(nota));
    }

    public List<NotaCreditoDTO> liquidarNotas(LiquidarNotasRequestDTO dto) {
        if (dto.getNotaIds() == null || dto.getNotaIds().isEmpty()) {
            throw new IllegalStateException("Debe seleccionar al menos una nota a liquidar");
        }
        List<NotaCredito> notas = notaRepository.findAllById(dto.getNotaIds());
        if (notas.isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontraron notas a liquidar");
        }
        for (NotaCredito nota : notas) {
            if (nota.getEstado() == EstadoNotaCredito.PAGADA) {
                throw new IllegalStateException("La nota " + nota.getNumero() + " ya está pagada");
            }
            if (nota.getEstado() == EstadoNotaCredito.BLOQUEADA) {
                throw new IllegalStateException("La nota " + nota.getNumero() + " está bloqueada");
            }
        }

        Map<Long, BigDecimal> totalPorCredito = new LinkedHashMap<>();
        for (NotaCredito nota : notas) {
            totalPorCredito.merge(nota.getCredito().getId(), nota.getSaldo(), BigDecimal::add);
        }

        LocalDate fechaPago = dto.getFechaPago() != null ? dto.getFechaPago() : LocalDate.now();
        List<AbonoCredito> abonos = new ArrayList<>();
        // Folios liquidados por crédito, para reflejar el detalle en Pagos y Abonos
        Map<Long, List<String>> foliosPorCredito = new LinkedHashMap<>();
        for (NotaCredito nota : notas) {
            foliosPorCredito.computeIfAbsent(nota.getCredito().getId(), k -> new ArrayList<>()).add(nota.getNumero());
        }
        for (Map.Entry<Long, BigDecimal> entry : totalPorCredito.entrySet()) {
            Credito credito = creditoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Crédito no encontrado: " + entry.getKey()));
            String detalle = "Notas: " + String.join(", ", foliosPorCredito.getOrDefault(entry.getKey(), List.of()));
            String obs = dto.getNotas() != null && !dto.getNotas().isBlank()
                    ? dto.getNotas() + " | " + detalle
                    : detalle;
            AbonoCredito abono = AbonoCredito.builder()
                    .folioAbono(generarFolioAbono())
                    .credito(credito)
                    .monto(entry.getValue())
                    .fechaAbono(fechaPago)
                    .metodoPago(dto.getMetodoPago())
                    .referenciaPago(dto.getReferenciaPago())
                    .notas(obs)
                    .build();
            abonoRepository.save(abono);
            abonos.add(abono);
        }

        for (NotaCredito nota : notas) {
            nota.setEstado(EstadoNotaCredito.PAGADA);
        }
        notaRepository.saveAll(notas);

        // Línea revolvente: al pagar se restaura el disponible
        // (disponible = límite − deuda de notas no pagadas)
        for (Map.Entry<Long, BigDecimal> entry : totalPorCredito.entrySet()) {
            creditoRepository.findById(entry.getKey()).ifPresent(credito -> {
                BigDecimal pagado = credito.getMontoPagado() != null ? credito.getMontoPagado() : BigDecimal.ZERO;
                credito.setMontoPagado(pagado.add(entry.getValue()));
                BigDecimal deuda = notaRepository.findByCreditoIdOrderByCreatedAtDesc(credito.getId()).stream()
                        .filter(n -> n.getEstado() != EstadoNotaCredito.PAGADA)
                        .map(n -> n.getSaldo() != null ? n.getSaldo() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal limite = credito.getMontoTotal() != null ? credito.getMontoTotal() : BigDecimal.ZERO;
                credito.setSaldoPendiente(limite.subtract(deuda));
                creditoRepository.save(credito);
            });
        }

        return notas.stream().map(this::aDTO).toList();
    }

    public NotaCreditoDTO bloquear(Long id) {
        NotaCredito nota = notaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada: " + id));
        nota.setEstado(EstadoNotaCredito.BLOQUEADA);
        return aDTO(notaRepository.save(nota));
    }

    private String generarFolioAbono() {
        return "ABONO-" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        ) + "-" + (abonoRepository.count() + 1);
    }

    public NotaCreditoDTO desbloquear(Long id) {
        NotaCredito nota = notaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada: " + id));
        nota.setEstado(EstadoNotaCredito.ACTIVA);
        return aDTO(notaRepository.save(nota));
    }

    private void validarCreditoDisponible(Credito credito) {
        if (credito.getEstado() != EstadoCredito.ACTIVO) {
            throw new IllegalStateException("El crédito no está activo");
        }
        if (credito.getFechaVencimiento() != null && credito.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalStateException("El crédito " + credito.getFolioCredito()
                    + " está vencido desde el " + credito.getFechaVencimiento()
                    + ". No se puede registrar en este crédito.");
        }
    }

    private BigDecimal subtotalDe(ItemNotaCreditoDTO i) {
        if (i.getSubtotal() != null) return i.getSubtotal();
        BigDecimal cantidad = i.getCantidad();
        BigDecimal precio = i.getPrecioUnitario();
        return (cantidad == null || precio == null) ? BigDecimal.ZERO : cantidad.multiply(precio);
    }

    private String nombreClienteDe(Credito credito) {
        if (credito == null || credito.getCliente() == null) return null;
        String razon = credito.getCliente().getRazonSocial();
        return (razon != null && !razon.isBlank()) ? razon : credito.getCliente().getNombre();
    }

    private String generarNumero() {
        return "NOTA-" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        ) + "-" + (notaRepository.count() + 1);
    }
}