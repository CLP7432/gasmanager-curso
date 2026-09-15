package com.gasmanager.facturacion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "facturacion")
public class FacturacionProperties {

    private Emisor emisor = new Emisor();
    private Cfdi cfdi = new Cfdi();

    @Getter
    @Setter
    public static class Emisor {
        private String rfc;
        private String razonSocial;
        private String regimenFiscal;
        private String codigoPostal;
    }

    @Getter
    @Setter
    public static class Cfdi {
        private String serie = "FAC";
        private String formaPagoPorDefecto = "01";
        private String metodoPagoPorDefecto = "PUE";
        private boolean simulacion = true;
        private BigDecimal ivaTasa = new BigDecimal("0.16");
        private BigDecimal iepsTasa = BigDecimal.ZERO;
        private String usoCfdiPorDefecto = "G01";
        private boolean complementoHidrocarburosHabilitado = false;
        private Claves claves = new Claves();
        private Hidrocarburos hidrocarburos = new Hidrocarburos();
        private Documentos documentos = new Documentos();
    }

    @Getter
    @Setter
    public static class Claves {
        private String claveProdServRegular = "15101514";
        private String claveProdServPremium = "15101515";
        private String claveProdServDiesel = "15101505";
        private String claveProdServAceite = "15111515";
        private String claveUnidadLitro = "LTR";
        private String claveUnidadPieza = "H87";
        private String claveUnidadLitroNombre = "Litro";
        private String claveUnidadPiezaNombre = "Pieza";
    }

    @Getter
    @Setter
    public static class Hidrocarburos {
        private String version = "1.1";
        private String tipoOperacion = "02";
        private String tipoServicio = "06";
        private String claveInstalacion = "06";
        private String numeroPermiso;
    }

    @Getter
    @Setter
    public static class Documentos {
        private String ruta = "facturadas";
    }
}