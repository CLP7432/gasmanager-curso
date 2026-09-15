package com.gasmanager.clientes.services;

import com.gasmanager.clientes.dto.AbonoCreditoDTO;
import com.gasmanager.clientes.dto.CreditoDTO;
import com.gasmanager.clientes.entities.AbonoCredito;
import com.gasmanager.clientes.entities.Cliente;
import com.gasmanager.clientes.entities.Credito;
import com.gasmanager.clientes.entities.NotaCredito;
import com.gasmanager.clientes.enums.EstadoCredito;
import com.gasmanager.clientes.enums.EstadoNotaCredito;
import com.gasmanager.clientes.exceptions.RecursoNoEncontradoException;
import com.gasmanager.clientes.repositories.AbonoCreditoRepository;
import com.gasmanager.clientes.repositories.ClienteRepository;
import com.gasmanager.clientes.repositories.CreditoRepository;
import com.gasmanager.clientes.repositories.NotaCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreditoService {

    private final CreditoRepository creditoRepository;
    private final ClienteRepository clienteRepository;
    private final AbonoCreditoRepository abonoRepository;
    private final NotaCreditoRepository notaRepository;

    public CreditoDTO crearCredito(CreditoDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no existe: " + dto.getClienteId()));
        if (!cliente.getActivo()) {
            throw new IllegalStateException("Cliente inactivo");
        }
        Credito c = Credito.builder()
                .folioCredito(generarFolio())
                .cliente(cliente)
                .montoTotal(dto.getMontoTotal())
                .saldoPendiente(dto.getMontoTotal())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoCredito.ACTIVO)
                .plazoMeses(dto.getPlazoMeses())
                .tasaInteres(dto.getTasaInteres())
                .montoInteres(dto.getMontoInteres() != null
                        ? dto.getMontoInteres()
                        : (dto.getMontoTotal() != null && dto.getTasaInteres() != null
                            ? dto.getMontoTotal().multiply(dto.getTasaInteres()).divide(new BigDecimal("100"))
                            : null))
                .fechaInicio(dto.getFechaInicio())
                .fechaVencimiento(dto.getFechaVencimiento())
                .metodoPago(dto.getMetodoPago())
                .diaPago(dto.getDiaPago())
                .notas(dto.getNotas())
                .build();
        return aDTO(creditoRepository.save(c));

    }

    public CreditoDTO registrarAbono(Long creditoId, AbonoCreditoDTO dto) {
        Credito credito = creditoRepository.findById(creditoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Credito no existe: " + creditoId
                ));
        BigDecimal deuda = deudaActiva(creditoId);
        if (dto.getMonto().compareTo(deuda) > 0) {
            throw new IllegalStateException("Abono supera la deuda pendiente ($" + deuda + ")");
        }
        AbonoCredito abono = AbonoCredito.builder()
                .folioAbono(generarFolioAbono())
                .credito(credito)
                .monto(dto.getMonto())
                .fechaAbono(dto.getFechaAbono())
                .metodoPago(dto.getMetodoPago())
                .referenciaPago(dto.getReferenciaPago())
                .notas(dto.getNotas())
                .build();
        credito.addAbono(abono);
        // Línea revolvente: el disponible se restaura al pagar
        credito.setSaldoPendiente(disponibleDe(credito));
        CreditoDTO dtoResultado = aDTO(creditoRepository.save(credito));
        if (credito.getEstado() == EstadoCredito.PAGADO) {
            marcarNotasPagadas(credito.getId());
        }
        return dtoResultado;
    }

    private void marcarNotasPagadas(Long creditoId) {
        notaRepository.findByCreditoIdOrderByCreatedAtDesc(creditoId)
                .stream()
                .filter(n -> n.getEstado() != EstadoNotaCredito.PAGADA)
                .forEach(n -> {
                    n.setEstado(EstadoNotaCredito.PAGADA);
                    notaRepository.save(n);
                });
    }

    public List<CreditoDTO> listarPorCliente(Long clienteId) {
        return creditoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public CreditoDTO obtener(Long id) {
        return aDTO(creditoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe " + id)));
    }

    public List<CreditoDTO> listarTodos() {
        return creditoRepository.findAll()
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public List<CreditoDTO> listarPorEstado(EstadoCredito estado) {
        return creditoRepository.findByEstado(estado)
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public List<CreditoDTO> listarActivosConSaldo() {
        LocalDate hoy = LocalDate.now();
        return creditoRepository.findByEstado(EstadoCredito.ACTIVO).stream()
                .filter(c -> c.getFechaVencimiento() == null || !c.getFechaVencimiento().isBefore(hoy))
                .filter(c -> disponibleDe(c).compareTo(BigDecimal.ZERO) > 0)
                .map(this::aDTO)
                .toList();
    }

    /**
     * Modelo revolvente: disponible = límite − deuda (notas no pagadas).
     * Al pagar, el disponible se restaura; la línea sigue ACTIVA.
     */
    public BigDecimal deudaActiva(Long creditoId) {
        return notaRepository.findByCreditoIdOrderByCreatedAtDesc(creditoId).stream()
                .filter(n -> n.getEstado() != EstadoNotaCredito.PAGADA)
                .map(n -> n.getSaldo() != null ? n.getSaldo() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal disponibleDe(Credito c) {
        BigDecimal limite = c.getMontoTotal() != null ? c.getMontoTotal() : BigDecimal.ZERO;
        return limite.subtract(deudaActiva(c.getId()));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void corregirSaldosAlArrancar() {
        try {
            int tocados = recalcularSaldos();
            if (tocados > 0) log.info("Saldos de crédito corregidos: {}", tocados);
        } catch (Exception e) {
            log.warn("No se pudieron corregir saldos al arrancar: {}", e.getMessage());
        }
    }

    /**
     * Sincroniza el saldo guardado con el modelo revolvente
     * (disponible = límite − deuda). La línea sigue ACTIVA al pagar.
     */
    @Transactional
    public int recalcularSaldos() {
        List<Credito> creditos = creditoRepository.findAll();
        int tocados = 0;
        for (Credito c : creditos) {
            BigDecimal pagado = c.getAbonos() == null ? BigDecimal.ZERO
                    : c.getAbonos().stream()
                        .map(a -> a.getMonto() != null ? a.getMonto() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal disponible = disponibleDe(c);
            boolean cambio = !pagado.equals(c.getMontoPagado()) || !disponible.equals(c.getSaldoPendiente());
            c.setMontoPagado(pagado);
            c.setSaldoPendiente(disponible);
            if (cambio) {
                creditoRepository.save(c);
                tocados++;
            }
        }
        return tocados;
    }

    public List<CreditoDTO> listarVencidos() {

        return creditoRepository.findByFechaVencimientoBeforeAndEstado(LocalDate.now(), EstadoCredito.ACTIVO)
                .stream()
                .map(this::aDTO)
                .toList();
    }

    public List<AbonoCreditoDTO> listarAbonos(Long creditoId) {
        return abonoRepository.findByCreditoIdOrderByFechaAbonoDesc(creditoId)
                .stream()
                .map(this::aAbonoDTO)
                .toList();
    }

    public CreditoDTO cancelarCredito(Long id, String motivo) {
        Credito credito = creditoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Credito no existe: " + id));

        credito.setEstado(EstadoCredito.CANCELADO);

        if (motivo != null && !motivo.isBlank()) {
            credito.setNotas((credito.getNotas() == null ? "" : credito.getNotas() + " ")
                    + "CANCELADO: " + motivo);
        }
        return aDTO(creditoRepository.save(credito));
    }

    public CreditoDTO actualizarCredito(Long id, CreditoDTO dto) {
        Credito credito = creditoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Credito no existe: " + id));

        credito.setMontoTotal(dto.getMontoTotal());
        credito.setPlazoMeses(dto.getPlazoMeses());
        credito.setTasaInteres(dto.getTasaInteres());
        credito.setMontoInteres(dto.getMontoInteres());
        credito.setFechaInicio(dto.getFechaInicio());
        credito.setFechaVencimiento(dto.getFechaVencimiento());
        credito.setMetodoPago(dto.getMetodoPago());
        credito.setDiaPago(dto.getDiaPago());
        credito.setNotas(dto.getNotas());
        return aDTO(creditoRepository.save(credito));
    }

    private String generarFolio() {
        return "CRED-" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        ) + "-" + (creditoRepository.count() + 1);
    }

    private String generarFolioAbono() {
        return "ABONO-" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        ) + "-" + (abonoRepository.count() + 1);
    }

    public List<AbonoCreditoDTO> listarTodosAbonos() {
        return abonoRepository.findAllByOrderByFechaAbonoDesc()
                .stream()
                .map(this::aAbonoDTO)
                .toList();
    }

    private CreditoDTO aDTO(Credito c) {
        return CreditoDTO.builder()
                .id(c.getId())
                .folioCredito(c.getFolioCredito())
                .clienteId(c.getCliente().getId())
                .clienteNombre(c.getCliente().getRazonSocial() != null && !c.getCliente().getRazonSocial().isBlank()
                        ? c.getCliente().getRazonSocial()
                        : c.getCliente().getNombre())
                .montoTotal(c.getMontoTotal())
                .montoPagado(c.getMontoPagado())
                .saldoPendiente(disponibleDe(c))
                .plazoMeses(c.getPlazoMeses())
                .tasaInteres(c.getTasaInteres())
                .montoInteres(c.getMontoInteres())
                .fechaInicio(c.getFechaInicio())
                .fechaVencimiento(c.getFechaVencimiento())
                .fechaUltimoPago(c.getFechaUltimoPago())
                .estado(c.getEstado())
                .metodoPago(c.getMetodoPago())
                .diaPago(c.getDiaPago())
                .notas(c.getNotas())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdateAt())
                .abonos(c.getAbonos() == null ? List.of() :
                        c.getAbonos().stream().map(this::aAbonoDTO).toList())
                .build();
    }

    private AbonoCreditoDTO aAbonoDTO(AbonoCredito a) {
        return AbonoCreditoDTO.builder()
                .id(a.getId())
                .folioAbono(a.getFolioAbono())
                .creditoId(a.getCredito().getId())
                .creditoFolio(a.getCredito().getFolioCredito())
                .monto(a.getMonto())
                .fechaAbono(a.getFechaAbono())
                .metodoPago(a.getMetodoPago())
                .referenciaPago(a.getReferenciaPago())
                .notas(a.getNotas())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
