package com.gasmanager.ia.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gasmanager.ia.dto.ChatRequestDTO;
import com.gasmanager.ia.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Asistente GasManager con Ollama local (sin API keys, funciona offline).
 * Enriquece el prompt con DATOS REALES según el módulo (contexto) y las
 * palabras clave del mensaje: explica, analiza datos y sugiere acciones.
 */
@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:qwen2.5-coder:7b}")
    private String ollamaModel;

    @Value("${microservicio.ventas.url:http://localhost:8095}")
    private String ventasUrl;

    @Value("${microservicio.clientes.url:http://localhost:8093}")
    private String clientesUrl;

    @Value("${microservicio.inventarios.url:http://localhost:8094}")
    private String inventariosUrl;

    @Value("${microservicio.nomina.url:http://localhost:8096}")
    private String nominaUrl;

    @Value("${microservicio.facturacion.url:http://localhost:8092}")
    private String facturacionUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OllamaService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ChatResponseDTO consultarIA(ChatRequestDTO request) {
        try {
            String datosContexto = obtenerDatosRelevantes(request);
            String prompt = construirPromptConContexto(request, datosContexto);
            String respuesta = llamarOllama(prompt);

            return ChatResponseDTO.builder()
                    .respuesta(respuesta)
                    .modelo(ollamaModel + " (local)")
                    .timestamp(LocalDateTime.now())
                    .exito(true)
                    .build();
        } catch (Exception e) {
            log.error("Error en consultarIA: {}", e.getMessage(), e);
            return ChatResponseDTO.builder()
                    .respuesta("Lo siento, en este momento no puedo procesar tu consulta. Verifica que Ollama esté corriendo e intenta más tarde.")
                    .timestamp(LocalDateTime.now())
                    .exito(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    private String obtenerDatosRelevantes(ChatRequestDTO request) {
        StringBuilder datos = new StringBuilder();
        String msg = request.getMensaje() != null ? request.getMensaje().toLowerCase() : "";
        String ctx = request.getContexto() != null ? request.getContexto().toUpperCase() : "GENERAL";

        try {
            // VENTAS: turno abierto + ventas del turno
            if (ctx.contains("VENTA") || ctx.contains("CORTE") || ctx.contains("TURNO")
                    || msg.contains("turno") || msg.contains("venta") || msg.contains("corte")
                    || msg.contains("despachador") || msg.contains("magna") || msg.contains("premium") || msg.contains("diesel")) {
                datos.append(obtenerTurnoAbierto());
            }
            // CLIENTES: créditos con saldo
            if (ctx.contains("CLIENTE") || msg.contains("crédit") || msg.contains("credito")
                    || msg.contains("nota") || msg.contains("pago") || msg.contains("liquidar") || msg.contains("cliente")) {
                datos.append(obtenerCreditos());
            }
            // INVENTARIOS: stock bajo en tanques y aceites
            if (ctx.contains("INVENTARIO") || ctx.contains("COMPRA") || msg.contains("stock")
                    || msg.contains("tanque") || msg.contains("aceite") || msg.contains("combustible")
                    || msg.contains("proveedor") || msg.contains("compra")) {
                datos.append(obtenerStockBajo());
            }
            // NOMINA: incidencias y empleados
            if (ctx.contains("NOMINA") || msg.contains("nómina") || msg.contains("nomina")
                    || msg.contains("empleado") || msg.contains("incidencia") || msg.contains("faltante")) {
                datos.append(obtenerNomina());
            }
            // FACTURACION: facturas recientes
            if (ctx.contains("FACTURA") || msg.contains("factura") || msg.contains("cfdi")
                    || msg.contains("timbr") || msg.contains("fiscal")) {
                datos.append(obtenerFacturas());
            }
        } catch (Exception e) {
            log.warn("Error obteniendo datos relevantes: {}", e.getMessage());
            datos.append("No se pudieron obtener datos en tiempo real. ");
        }

        return datos.toString();
    }

    private String obtenerTurnoAbierto() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    ventasUrl + "/api/turnos?estado=ABIERTO", String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && root.size() > 0) {
                    JsonNode turno = root.get(0);
                    long turnoId = turno.path("id").asLong();
                    StringBuilder sb = new StringBuilder("\n\nDATOS REALES - TURNO ABIERTO:\n");
                    sb.append("Código: ").append(turno.path("codigoTurno").asText()).append("\n");
                    sb.append("Nombre: ").append(turno.path("nombre").asText()).append("\n");
                    sb.append("Inicio: ").append(turno.path("horaInicio").asText()).append("\n");
                    sb.append(obtenerVentasDelTurno(turnoId));
                    return sb.toString();
                }
                return "\n\nDATOS REALES - TURNO ABIERTO:\nNo hay ningún turno abierto actualmente.\n";
            }
        } catch (Exception e) {
            log.warn("Error obteniendo turno abierto: {}", e.getMessage());
        }
        return "";
    }

    private String obtenerVentasDelTurno(long turnoId) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    ventasUrl + "/api/ventas/turno/" + turnoId, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    double total = 0;
                    int n = 0;
                    for (JsonNode v : root) {
                        if (!"CANCELADA".equalsIgnoreCase(v.path("estado").asText())) {
                            total += v.path("total").asDouble();
                            n++;
                        }
                    }
                    return "Ventas del turno: " + n + ", total $" + String.format("%.2f", total) + "\n";
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo ventas del turno: {}", e.getMessage());
        }
        return "";
    }

    private String obtenerCreditos() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    clientesUrl + "/api/creditos/activos-con-saldo", String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    double saldo = 0;
                    for (JsonNode
                            c : root) saldo += c.path("saldoPendiente").asDouble();
                    StringBuilder sb = new StringBuilder("\n\nDATOS REALES - CRÉDITOS:\n");
                    sb.append("Créditos activos con saldo: ").append(root.size()).append("\n");
                    sb.append("Saldo pendiente total: $").append(String.format("%.2f", saldo)).append("\n");
                    int i = 0;
                    for (JsonNode c : root) {
                        if (i++ >= 5) break;
                        sb.append("  - ").append(c.path("folioCredito").asText()).append(" (")
                                .append(c.path("clienteNombre").asText()).append("): $")
                                .append(String.format("%.2f", c.path("saldoPendiente").asDouble())).append("\n");
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo créditos: {}", e.getMessage());
        }
        return "";
    }

    private String obtenerStockBajo() {
        StringBuilder sb = new StringBuilder("\n\nDATOS REALES - INVENTARIO:\n");
        boolean conDatos = false;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    inventariosUrl + "/api/aceites", String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    int bajos = 0;
                    StringBuilder detalle = new StringBuilder();
                    for (JsonNode a : root) {
                        int stock = a.path("stockActual").asInt(0);
                        int min = a.path("stockMinimo").asInt(0);
                        if (stock <= min) {
                            bajos++;
                            detalle.append("  - ").append(a.path("nombre").asText())
                                    .append(": ").append(stock).append(" pzas (mín ").append(min).append(")\n");
                        }
                    }
                    sb.append("Aceites con stock bajo: ").append(bajos).append("\n").append(detalle);
                    conDatos = true;
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo aceites: {}", e.getMessage());
        }
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    inventariosUrl + "/api/tanques", String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    sb.append("Tanques: ").append(root.size()).append("\n");
                    for (JsonNode t : root) {
                        double cap = t.path("capacidadLitros").asDouble(0);
                        double stock = t.path("stockLitros").asDouble(0);
                        double pct = cap > 0 ? stock * 100 / cap : 0;
                        if (pct < 25) {
                            sb.append("  - ").append(t.path("nombre").asText())
                                    .append(": ").append(String.format("%.0f", stock)).append(" L (")
                                    .append(String.format("%.0f", pct)).append("%)\n");
                        }
                    }
                    conDatos = true;
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo tanques: {}", e.getMessage());
        }
        return conDatos ? sb.toString() : "";
    }

    private String obtenerNomina() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    nominaUrl + "/api/incidencias", String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    return "\n\nDATOS REALES - NÓMINA:\nIncidencias registradas: " + root.size() + "\n";
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo incidencias: {}", e.getMessage());
        }
        return "";
    }

    private String obtenerFacturas() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    facturacionUrl + "/api/facturas", String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray()) {
                    double total = 0;
                    for (JsonNode f : root) {
                        if (!"CANCELADA".equalsIgnoreCase(f.path("estado").asText())) {
                            total += f.path("total").asDouble();
                        }
                    }
                    return "\n\nDATOS REALES - FACTURACIÓN:\nFacturas emitidas: " + root.size()
                            + ", total $" + String.format("%.2f", total) + "\n";
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo facturas: {}", e.getMessage());
        }
        return "";
    }

    private String descripcionModulo(String contexto) {
        if (contexto == null) return "del sistema GasManager";
        switch (contexto.toUpperCase()) {
            case "VENTAS": return "de Ventas (punto de venta, turnos, dispensarios, cortes)";
            case "CLIENTES": return "de Clientes (registro, créditos, notas de crédito y pagos)";
            case "INVENTARIOS": return "de Inventarios (aceites, combustibles, tanques, compras y proveedores)";
            case "NOMINA": return "de Nómina (empleados, puestos, incidencias y nóminas)";
            case "FACTURACION": return "de Facturación (CFDI, clientes fiscales y notas por facturar)";
            case "ADMIN": return "de Administración (usuarios, roles, permisos y auditoría)";
            case "COMPRAS": return "de Compras (facturas de compra y reporte)";
            default: return "del sistema GasManager";
        }
    }

    private String construirPromptConContexto(ChatRequestDTO request, String datosReales) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("El usuario está en el módulo ").append(descripcionModulo(request.getContexto())).append(". ");
        prompt.append("Usuario: ").append(request.getUsuarioNombre() != null ? request.getUsuarioNombre() : "Usuario");
        if (!datosReales.isEmpty()) {
            prompt.append(datosReales);
        }
        prompt.append("\n\nCONSULTA DEL USUARIO: ").append(request.getMensaje());
        prompt.append("\n\nResponde usando los DATOS REALES cuando apliquen. Si no hay datos, dilo y ofrece cómo obtenerlos en el sistema.");
        return prompt.toString();
    }

    private String llamarOllama(String prompt) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", ollamaModel);
        body.put("system", "Eres GasManager Assistant, ayuda para una gasolinera mexicana. Explicas pantallas, analizas datos operativos y sugieres acciones concretas. Responde en español, claro y conciso (máximo 150 palabras), sin formato markdown complejo.");
        body.put("prompt", prompt);
        body.put("stream", false);
        body.put("options", Map.of("temperature", 0.3, "num_predict", 512));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        int maxIntentos = 2;
        Exception ultimoError = null;
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        ollamaUrl + "/api/generate", HttpMethod.POST, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    String texto = root.path("response").asText("").trim();
                    if (!texto.isEmpty()) return texto;
                }
            } catch (Exception e) {
                ultimoError = e;
                log.warn("Ollama intento {}/{}: {}", intento, maxIntentos, e.getMessage());
                Thread.sleep(2000L);
            }
        }
        throw ultimoError != null ? ultimoError : new RuntimeException("Ollama no respondió");
    }
}
