import React, {useEffect, useState} from "react";
import {facturasService} from "../../api/facturacion/auth.js";
import CardBase from "../../kernel/components/CardBase.jsx";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

// Vista de factura como representación impresa de CFDI.
// Los botones llevan clase no-print: no salen en la impresión.
const FacturaVista = ({factura, onCerrar}) => {
    const [emisor, setEmisor] = useState(null);
    const [enviando, setEnviando] = useState(false);
    const [correoOk, setCorreoOk] = useState(!!factura.correoEnviado);

    useEffect(() => {
        facturasService.emisor().then(setEmisor).catch(() => setEmisor(null));
    }, []);

    const exportarPdf = async () => {
        try {
            const blob = await facturasService.pdf(factura.id);
            const url = window.URL.createObjectURL(new Blob([blob]));
            const a = document.createElement('a');
            a.href = url; a.download = `${factura.folio}.pdf`;
            a.click();
        } catch {
            alert('No se pudo exportar el PDF');
        }
    };

    const enviarCorreo = async () => {
        setEnviando(true);
        try {
            const r = await facturasService.enviarCorreo(factura.id);
            alert(r.mensaje || 'Correo procesado');
            setCorreoOk(true);
        } catch (error) {
            alert(error.response?.data?.message || 'No se pudo enviar el correo');
        }
        setEnviando(false);
    };

    return (
        <CardBase titulo={`Factura ${factura.folio}`} style={{maxWidth: '900px', padding: '1rem', marginBottom: '15px'}}>
            <div className="no-print d-flex gap-2 flex-wrap mb-3">
                <button className="btn btn-primary" onClick={() => window.print()}>🖨️ Imprimir</button>
                <button className="btn btn-outline-secondary" onClick={exportarPdf}>⬇️ Exportar PDF</button>
                <button className="btn btn-outline-info" disabled={enviando || correoOk} onClick={enviarCorreo}>
                    {correoOk ? '✉️ Correo enviado' : enviando ? 'Enviando...' : '✉️ Enviar por correo'}
                </button>
                <button className="btn btn-secondary" onClick={onCerrar}>Cerrar</button>
            </div>
            <div className="factura-print-area" style={{color: '#000', background: '#fff'}}>
                <div style={{display: 'flex', justifyContent: 'space-between', borderBottom: '3px solid #0f172a', paddingBottom: '10px', marginBottom: '10px'}}>
                    <div>
                        <h3 style={{margin: 0}}>⛽ GasManager</h3>
                        <div><strong>{emisor?.razonSocial || 'Estación de servicio'}</strong></div>
                        <div>RFC: {emisor?.rfc || '-'}</div>
                        <div>Régimen: {emisor?.regimenFiscal || '-'} · C.P. {emisor?.codigoPostal || '-'}</div>
                    </div>
                    <div style={{textAlign: 'right'}}>
                        <h4 style={{margin: 0}}>FACTURA {factura.folio}</h4>
                        <div>Serie: {factura.serie || '-'}</div>
                        <div>Fecha: {factura.fechaEmision || '-'}</div>
                        <div>UUID: {factura.uuid || '(sin timbrar)'}</div>
                        <div>Estado: <strong>{factura.estado}{factura.timbrada ? '' : ' · Sin timbrar'}</strong></div>
                    </div>
                </div>
                <div style={{display: 'flex', gap: '20px', marginBottom: '10px'}}>
                    <div style={{flex: 1, border: '1px solid #ccc', padding: '8px'}}>
                        <strong>Receptor</strong>
                        <div>{factura.receptorNombre}</div>
                        <div>RFC: {factura.receptorRfc}</div>
                        <div>Régimen: {factura.receptorRegimenFiscal} · C.P. {factura.receptorCodigoPostal}</div>
                        <div>Uso CFDI: {factura.receptorUsoCfdi}</div>
                    </div>
                    <div style={{flex: 1, border: '1px solid #ccc', padding: '8px'}}>
                        <strong>Pago</strong>
                        <div>Forma de pago: {factura.formaPago}</div>
                        <div>Método de pago: {factura.metodoPago}</div>
                    </div>
                </div>
                <table className="table table-sm table-bordered">
                    <thead>
                    <tr>
                        <th>Cant.</th>
                        <th>Unidad</th>
                        <th>Clave</th>
                        <th>Descripción</th>
                        <th className="text-end">P. Unit.</th>
                        <th className="text-end">Importe</th>
                    </tr>
                    </thead>
                    <tbody>
                    {(factura.conceptos || []).map((c, i) => (
                        <tr key={i}>
                            <td>{c.cantidad}</td>
                            <td>{c.unidad}</td>
                            <td>{c.claveProdServ}</td>
                            <td>{c.descripcion} <small className="text-muted">({c.origen}{c.origenFolio ? ` ${c.origenFolio}` : ''})</small></td>
                            <td className="text-end">{dinero(c.valorUnitario)}</td>
                            <td className="text-end">{dinero(c.importe)}</td>
                        </tr>
                    ))}
                    </tbody>
                    <tfoot>
                    <tr>
                        <th colSpan="5" className="text-end">Subtotal</th>
                        <th className="text-end">{dinero(factura.subtotal)}</th>
                    </tr>
                    <tr>
                        <th colSpan="5" className="text-end">IVA</th>
                        <th className="text-end">{dinero(factura.iva)}</th>
                    </tr>
                    <tr>
                        <th colSpan="5" className="text-end">Total</th>
                        <th className="text-end">{dinero(factura.total)}</th>
                    </tr>
                    </tfoot>
                </table>
                <p style={{fontSize: '11px', color: '#555', marginTop: '8px'}}>
                    Este documento es una representación impresa de un CFDI{factura.timbrada ? '' : ' (sin timbrar)'}.
                    Folio fiscal (UUID): {factura.uuid || 'pendiente'}.
                </p>
            </div>
        </CardBase>
    );
};
export default FacturaVista;
