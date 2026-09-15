package com.gasmanager.facturacion.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorreoService {

    public boolean enviar(String destino, String asunto, String texto, byte[] pdf, byte[] xml) {
        log.info("ENVÍO DE CORREO SIMULADO -> to={} asunto={}", destino, asunto);
        log.info("Adjuntos: pdf={} bytes, xml={} bytes", pdf != null ? pdf.length : 0, xml != null ? xml.length : 0);
        return false;
    }
}