package com.gasmanager.facturacion.services;

import com.gasmanager.facturacion.config.FacturacionProperties;
import com.gasmanager.facturacion.entities.Factura;
import com.gasmanager.facturacion.entities.FacturaConcepto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private static final String PRECIO = "#,##0.00";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final FacturacionProperties props;

    public byte[] generarPdf(Factura factura) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            encabezado(doc, factura);
            tablasTotales(doc, factura);
            totales(doc, factura);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo generar el PDF de la factura: " + e.getMessage());
        }
    }

    private void encabezado(Document doc, Factura f) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);

        PdfPCell izq = new PdfPCell();
        izq.setBorder(Rectangle.BOX);
        izq.addElement(new Paragraph(props.getEmisor().getRazonSocial(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
        izq.addElement(new Paragraph("RFC: " + props.getEmisor().getRfc()));
        izq.addElement(new Paragraph("Régimen: " + props.getEmisor().getRegimenFiscal()));

        PdfPCell der = new PdfPCell();
        der.setBorder(Rectangle.BOX);
        der.addElement(new Paragraph("FACTURA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLUE)));
        der.addElement(new Paragraph("Folio: " + f.getFolio()));
        der.addElement(new Paragraph("UUID: " + f.getUuid()));
        der.addElement(new Paragraph("Fecha: " + f.getFechaEmision().format(FECHA)));
        der.addElement(new Paragraph("Tipo: " + (f.isTimbrada() ? "Timbre fiscal digital (CFDI 4.0)" : "Simulación sin timbre (CFDI 4.0)")));

        t.addCell(izq);
        t.addCell(der);
        doc.add(t);
        doc.add(new Paragraph("\n"));
    }

    private void tablasTotales(Document doc, Factura f) throws DocumentException {
        PdfPTable receptor = new PdfPTable(2);
        receptor.setWidthPercentage(100);
        receptor.addCell(celda("Receptor: " + nullable(f.getReceptorNombre()), true));
        receptor.addCell(celda("RFC receptor: " + f.getReceptorRfc(), true));
        receptor.addCell(celda("Régimen: " + nullable(f.getReceptorRegimenFiscal()), false));
        receptor.addCell(celda("CP: " + nullable(f.getReceptorCodigoPostal()) + "  |  Uso CFDI: " + nullable(f.getReceptorUsoCfdi()), false));
        doc.add(receptor);
        doc.add(new Paragraph("\n"));

        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{2.4f, 1f, 1.2f, 1.2f, 1.1f, 1.1f});
        for (String cab : new String[]{"Descripción", "Cant.", "U.M.", "P. Unit.", "IVA", "Importe"}) {
            tabla.addCell(celdaCabecera(cab));
        }
        for (FacturaConcepto c : f.getConceptos()) {
            tabla.addCell(cap(c.getDescripcion()));
            tabla.addCell(cap(str(c.getCantidad())));
            tabla.addCell(cap(nullable(c.getUnidad())));
            tabla.addCell(cap(money(c.getValorUnitario(), 4)));
            tabla.addCell(cap(money(c.getIva(), 2)));
            tabla.addCell(cap(money(c.getImporte(), 2)));
        }
        doc.add(tabla);
        doc.add(new Paragraph("\n"));
    }

    private void totales(Document doc, Factura f) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(60);
        t.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(celda("Subtotal", true));
        t.addCell(celda(money(f.getSubtotal(), 2), true));
        t.addCell(celda("IVA", false));
        t.addCell(celda(money(f.getIva(), 2), false));
        if (f.getIeps() != null && f.getIeps().compareTo(java.math.BigDecimal.ZERO) > 0) {
            t.addCell(celda("IEPS", false));
            t.addCell(celda(money(f.getIeps(), 2), false));
        }
        t.addCell(celda("TOTAL", true));
        t.addCell(celda(money(f.getTotal(), 2), true));
        doc.add(t);
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Método de pago: " + nullable(f.getMetodoPago()) + "   Forma de pago: " + nullable(f.getFormaPago())));
        doc.add(new Paragraph("Documento generado en modo simulación, sin timbre fiscal digital."));
    }

    private PdfPCell celda(String texto, boolean bold) {
        PdfPCell c = new PdfPCell(new Paragraph(texto, FontFactory.getFont(FontFactory.HELVETICA, 9, bold ? Font.BOLD : Font.NORMAL)));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(2f);
        return c;
    }

    private PdfPCell celdaCabecera(String texto) {
        PdfPCell c = new PdfPCell(new Paragraph(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        c.setBackgroundColor(new Color(15, 23, 42));
        c.setPadding(4f);
        return c;
    }

    private PdfPCell cap(String texto) {
        PdfPCell c = new PdfPCell(new Paragraph(texto, FontFactory.getFont(FontFactory.HELVETICA, 8)));
        c.setPadding(3f);
        return c;
    }

    private String str(java.math.BigDecimal v) {
        return v == null ? "0" : v.stripTrailingZeros().toPlainString();
    }

    private String money(java.math.BigDecimal v, int decimales) {
        if (v == null) v = java.math.BigDecimal.ZERO;
        return String.format("$%,." + decimales + "f", v.setScale(decimales, RoundingMode.HALF_UP));
    }

    private String nullable(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}