package com.gasmanager.facturacion.controllers;

import com.gasmanager.facturacion.config.FacturacionProperties;
import com.gasmanager.facturacion.dto.CrearFacturaRequestDTO;
import com.gasmanager.facturacion.dto.DisponiblesResponseDTO;
import com.gasmanager.facturacion.dto.FacturaResponseDTO;
import com.gasmanager.facturacion.services.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService service;
    private final FacturacionProperties props;

    @GetMapping("/emisor")
    public ResponseEntity<FacturacionProperties.Emisor> emisor() {
        return ResponseEntity.ok(props.getEmisor());
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/cliente/{clienteFiscalId}")
    public ResponseEntity<List<FacturaResponseDTO>> listarPorCliente(@PathVariable Long clienteFiscalId) {
        return ResponseEntity.ok(service.listarPorCliente(clienteFiscalId));
    }

    @GetMapping("/disponibles/{clienteId}")
    public ResponseEntity<DisponiblesResponseDTO> disponibles(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.disponibles(clienteId));
    }

    @PostMapping
    public ResponseEntity<FacturaResponseDTO> facturar(@Valid @RequestBody CrearFacturaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.facturar(request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<FacturaResponseDTO> cancelar(@PathVariable Long id,
                                                       @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(service.cancelar(id, motivo));
    }

    @GetMapping(value = "/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> xml(@PathVariable Long id) {
        byte[] xml = service.xml(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura-" + id + ".xml\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] pdf = service.pdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{id}/enviar-correo")
    public ResponseEntity<Map<String, Object>> enviarCorreo(@PathVariable Long id) {
        boolean enviado = service.enviarCorreo(id);
        return ResponseEntity.ok(Map.of(
                "enviado", enviado,
                "mensaje", enviado ? "Correo enviado correctamente"
                                   : "Envío de correo configurado en simulación: aún no se envía (falta SMTP)"
        ));
    }
}