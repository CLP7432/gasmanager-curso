package com.gasmanager.facturacion.services;

import com.gasmanager.facturacion.config.FacturacionProperties;
import com.gasmanager.facturacion.entities.Factura;
import com.gasmanager.facturacion.entities.FacturaConcepto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.RequiredArgsConstructor;
import mx.gob.sat.cfdi.catalogos.MetodoDePago;
import mx.gob.sat.cfdi.catalogos.Moneda;
import mx.gob.sat.cfdi.catalogos.TipoDeComprobante;
import mx.gob.sat.cfdi.catalogos.TipoDeFactor;
import mx.gob.sat.cfdi.catalogos.UsoDeCFDI;
import mx.gob.sat.cfdi.v40.Comprobante;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CfdiGeneratorService {

    private static final String NS_CFD = "http://www.sat.gob.mx/cfd/4";
    private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String NS_HIDRO = "http://www.sat.gob.mx/Hidrocarburos";
    private static final String XSD_CFD = "http://www.sat.gob.mx/sitio_internet/cfd/4/cfdv40.xsd";

    private final FacturacionProperties props;

    public String generarXml(Factura f) throws JAXBException {
        Comprobante c = new Comprobante();
        c.setSerie(f.getSerie());
        c.setFolio(f.getFolio());
        c.setFechaEmision(f.getFechaEmision());
        c.setFormaPago(f.getFormaPago());
        c.setSubTotal(f.getSubtotal());
        c.setMoneda(Moneda.MXN);
        c.setTotal(f.getTotal());
        c.setTipoDeComprobante(TipoDeComprobante.INGRESO);
        c.setExportacion("01");
        c.setMetodoPago(esDiferida(f) ? MetodoDePago.DIFERIDO : MetodoDePago.UNICO);
        c.setLugarExpedicion(props.getEmisor().getCodigoPostal());

        c.setEmisor(buildEmisor());
        c.setReceptor(buildReceptor(f));
        c.setConceptos(buildConceptos(f));
        c.setImpuestos(buildImpuestosComprobante(f));
        if (props.getCfdi().isComplementoHidrocarburosHabilitado() && tieneCombustibles(f)) {
            c.setComplemento(buildComplemento(f));
        }
        return marshall(c);
    }

    private  Comprobante.Emisor buildEmisor() {
        Comprobante.Emisor e = new Comprobante.Emisor();
        e.setRfc(props.getEmisor().getRfc());
        e.setNombre(props.getEmisor().getRazonSocial());
        e.setRegimenFiscal(props.getEmisor().getRegimenFiscal());
        return e;
    }

    private Comprobante.Receptor buildReceptor(Factura f) {
        Comprobante.Receptor r = new Comprobante.Receptor();
        r.setRfc(f.getReceptorRfc());
        r.setNombre(f.getReceptorNombre());
        r.setRegimenFiscalReceptor(f.getReceptorRegimenFiscal());
        r.setDomicilioFiscalReceptor(f.getReceptorCodigoPostal());
        r.setUsoCFDI(UsoDeCFDI.fromValue(f.getReceptorUsoCfdi()));
        return r;
    }

    private Comprobante.Conceptos buildConceptos(Factura f) {
        Comprobante.Conceptos conceptos = new Comprobante.Conceptos();
        List<FacturaConcepto> items = f.getConceptos();
        BigDecimal base;

        if (items.isEmpty() && esPagoEnParcialidades(f)) {
            base = f.getTotal();
            conceptos.getConcepto().add(conceptoLinea("R0000000-000001",
                    "0010100", "HUR", "PAGO EN PARCIALIDADES O DIFERIDO", base));
        } else {
            base = f.getSubtotal();
            for (FacturaConcepto fc : items) {
                conceptos.getConcepto().add(conceptoLinea(
                        fc.getClaveProdServ(), fc.getClaveUnidad(), fc.getUnidad(),
                        fc.getDescripcion(), fc.getImporte()));
            }
        }
        return conceptos;
    }

    private Comprobante.Conceptos.Concepto conceptoLinea(String clvProd, String clvUnidad,
                                                         String unidad, String desc, BigDecimal base) {
        Comprobante.Conceptos.Concepto k = new Comprobante.Conceptos.Concepto();
        k.setClaveProdServ(clvProd);
        k.setClaveUnidad(clvUnidad);
        k.setCantidad(BigDecimal.ONE);
        k.setUnidad(unidad);
        k.setDescripcion(desc);
        k.setValorUnitario(base);
        k.setImporte(base);
        k.setObjetoImp("02");

        Comprobante.Conceptos.Concepto.Impuestos imp = new Comprobante.Conceptos.Concepto.Impuestos();
        Comprobante.Conceptos.Concepto.Impuestos.Traslados tras = new Comprobante.Conceptos.Concepto.Impuestos.Traslados();
        Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado iva = new Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado();
        iva.setBase(base);
        iva.setImpuesto("002");
        iva.setTipoFactor(TipoDeFactor.TASA);
        iva.setTasaOCuota(props.getCfdi().getIvaTasa());
        iva.setImporte(base.multiply(props.getCfdi().getIvaTasa()));
        tras.getTraslado().add(iva);
        imp.setTraslados(tras);
        k.setImpuestos(imp);
        return k;
    }

    private Comprobante.Impuestos buildImpuestosComprobante(Factura f) {
        Comprobante.Impuestos i = new Comprobante.Impuestos();
        BigDecimal base = f.getConceptos().isEmpty() ? f.getTotal() : f.getSubtotal();
        BigDecimal iva = f.getTotal().subtract(base);

        Comprobante.Impuestos.Traslados tras = new Comprobante.Impuestos.Traslados();
        Comprobante.Impuestos.Traslados.Traslado t = new Comprobante.Impuestos.Traslados.Traslado();
        t.setBase(base);
        t.setImpuesto("002");
        t.setTipoFactor(TipoDeFactor.TASA);
        t.setTasaOCuota(props.getCfdi().getIvaTasa());
        t.setImporte(iva);
        tras.getTraslado().add(t);

        i.setTraslados(tras);
        i.setTotalImpuestosTrasladados(iva);
        return i;
    }

    private Comprobante.Complemento buildComplemento(Factura f) throws JAXBException {
        Comprobante.Complemento comp = new Comprobante.Complemento();
        comp.getAny().add(hidrocarburos(f));
        return comp;
    }

    private Element hidrocarburos(Factura f) throws JAXBException {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.newDocument();
            Element hidro = doc.createElementNS(NS_HIDRO, "Hidrocarburos");
            hidro.setAttribute("Version", "1.1");

            Element conceptosH = doc.createElementNS(NS_HIDRO, "ConceptosHidrocarburos");
            conceptosH.setAttribute("TotalConceptos", String.valueOf(f.getConceptos().size()));

            for (FacturaConcepto fc : f.getConceptos()) {
                Element ch = doc.createElementNS(NS_HIDRO, "ConceptoHidrocarburo");
                ch.setAttributeNS(NS_XSI, "xsi:schemaLocation",
                        NS_HIDRO + " http://www.sat.gob.mx/sitio_internet/cfd/Hidrocarburos/Hidrocarburos_1_1.xsd");
                ch.setAttribute("Identificador", String.valueOf(fc.getId()));
                ch.setAttribute("Fecha", f.getFechaEmision().toString());
                ch.setAttribute("ClaveProdServ", fc.getClaveProdServ());
                ch.setAttribute("ClaveUnidad", fc.getClaveUnidad());
                ch.setAttribute("Cantidad", fc.getCantidad().toPlainString());
                ch.setAttribute("ValorUnitario", fc.getValorUnitario().toPlainString());
                ch.setAttribute("Importe", fc.getImporte().toPlainString());
                conceptosH.appendChild(ch);
            }
            hidro.appendChild(conceptosH);
            return hidro;
        } catch (Exception e) {
            throw new JAXBException("No se pudo construir el complemento Hidrocarburos", e);
        }
    }

    private String marshall(Comprobante c) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(Comprobante.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        JAXBElement<Comprobante> root = new JAXBElement<>(
                new QName(NS_CFD, "Comprobante"), Comprobante.class, c);
        m.marshal(root, sw);
        String xml = sw.toString();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + xml.replaceFirst(java.util.regex.Pattern.quote("<Comprobante"),
                "<Comprobante xmlns:xsi=\"" + NS_XSI + "\""
                        + " xsi:schemaLocation=\"" + NS_CFD + " " + XSD_CFD + "\"");
    }

    private boolean esDiferida(Factura f) {
        return "PPD".equalsIgnoreCase(f.getMetodoPago());
    }

    private boolean esPagoEnParcialidades(Factura f) {
        return "PPD".equalsIgnoreCase(f.getMetodoPago());
    }

    private boolean tieneCombustibles(Factura f) {
        return f.getConceptos().stream().anyMatch(FacturaConcepto::isEsCombustible);
    }
}