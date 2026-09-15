package com.gasmanager.lealtad.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gasmanager.lealtad.dto.CuentaPuntosDTO;
import com.gasmanager.lealtad.dto.ProgramaDTO;
import com.gasmanager.lealtad.dto.PuntosVentaDTO;
import com.gasmanager.lealtad.entities.CuentaPuntos;
import com.gasmanager.lealtad.entities.TransaccionPuntos;
import com.gasmanager.lealtad.repositories.CuentaPuntosRepository;
import com.gasmanager.lealtad.repositories.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LealtadService {

    private static final Logger log = LoggerFactory.getLogger(LealtadService.class);

    private final CuentaPuntosRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final ProgramaService programaService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${microservicio.ventas.url:http://localhost:8095}")
    private String ventasUrl;

    /**
     * Acumula puntos de una venta. Si NO hay programa vigente devuelve 0 puntos
     * (no es error: la venta ya quedó registrada, simplemente no genera puntos).
     * Idempotente por venta: si el ticket ya acumuló, devuelve lo guardado.
     */
    @Transactional
    public PuntosVentaDTO acumular(Long ventaId) {
        Optional<CuentaPuntos> existente = cuentaRepository.findByVentaId(ventaId);
        if (existente.isPresent()) {
            CuentaPuntos c = existente.get();
            Optional<ProgramaDTO> prog = programaService.programaVigente();
            return PuntosVentaDTO.builder()
                    .ventaId(ventaId)
                    .folioVenta(c.getFolioVenta())
                    .puntos(c.getPuntos())
                    .programaNombre(prog.map(ProgramaDTO::getNombre).orElse(null))
                    .programaActivo(prog.isPresent())
                    .build();
        }

        Optional<ProgramaDTO> programa = programaService.programaVigente();
        if (programa.isEmpty()) {
            return PuntosVentaDTO.builder()
                    .ventaId(ventaId)
                    .puntos(0)
                    .programaActivo(false)
                    .build();
        }

        DatosVenta datos = leerVenta(ventaId);
        int puntos = (int) (datos.litros.doubleValue() * programa.get().getPuntosPorLitro());

        CuentaPuntos cuenta = cuentaRepository.save(CuentaPuntos.builder()
                .ventaId(ventaId)
                .folioVenta(datos.folio)
                .litros(datos.litros)
                .puntos(puntos)
                .build());

        transaccionRepository.save(TransaccionPuntos.builder()
                .ventaId(ventaId)
                .folioVenta(datos.folio)
                .programaId(programa.get().getId())
                .programaNombre(programa.get().getNombre())
                .fecha(LocalDateTime.now())
                .monto(datos.total)
                .litros(datos.litros)
                .puntos(puntos)
                .build());

        return PuntosVentaDTO.builder()
                .ventaId(ventaId)
                .folioVenta(datos.folio)
                .puntos(cuenta.getPuntos())
                .programaNombre(programa.get().getNombre())
                .programaActivo(true)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CuentaPuntosDTO> listarCuentas() {
        return cuentaRepository.findAll().stream()
                .map(c -> CuentaPuntosDTO.builder()
                        .id(c.getId())
                        .ventaId(c.getVentaId())
                        .folioVenta(c.getFolioVenta())
                        .litros(c.getLitros())
                        .puntos(c.getPuntos())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CuentaPuntosDTO> cuentaPorVenta(Long ventaId) {
        return cuentaRepository.findByVentaId(ventaId)
                .map(c -> CuentaPuntosDTO.builder()
                        .id(c.getId())
                        .ventaId(c.getVentaId())
                        .folioVenta(c.getFolioVenta())
                        .litros(c.getLitros())
                        .puntos(c.getPuntos())
                        .build());
    }

    private DatosVenta leerVenta(Long ventaId) {
        DatosVenta datos = new DatosVenta();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    ventasUrl + "/api/ventas/" + ventaId, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                datos.folio = root.path("folio").asText(null);
                datos.total = new BigDecimal(root.path("total").asText("0"));
                BigDecimal litros = BigDecimal.ZERO;
                JsonNode detalles = root.path("detalles");
                if (detalles.isArray()) {
                    for (JsonNode d : detalles) {
                        if ("COMBUSTIBLE".equalsIgnoreCase(d.path("tipoProducto").asText())) {
                            litros = litros.add(new BigDecimal(d.path("cantidad").asText("0")));
                        }
                    }
                }
                datos.litros = litros;
            }
        } catch (Exception e) {
            log.warn("No se pudo leer la venta {}: {}", ventaId, e.getMessage());
        }
        return datos;
    }

    private static class DatosVenta {
        String folio;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal litros = BigDecimal.ZERO;
    }
}
