import {ventasService} from "../ventas/auth.js";
import {comprasService} from "../inventarios/auth.js";
import {creditosService} from "../clients/auth.js";
import {facturasService} from "../facturacion/auth.js";

// Agregación en frontend (sin microservicio): junta las APIs existentes.
const enRango = (fechaHora, inicio, fin) => {
    if (!fechaHora) return false;
    const f = String(fechaHora).slice(0, 10);
    if (inicio && f < inicio) return false;
    if (fin && f > fin) return false;
    return true;
};

export const downloadBlob = (blob, filename) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
};

export const toCSV = (filas, columnas) => {
    const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`;
    const head = columnas.map(c => esc(c.label)).join(',');
    const body = filas.map(f => columnas.map(c => esc(c.get ? c.get(f) : f[c.key])).join(',')).join('\n');
    return '\ufeff' + head + '\n' + body;
};

export const reportesService = {
    ventasPorRango: async (inicio, fin, {estado = '', metodoPago = ''} = {}) => {
        const todas = await ventasService.listar().catch(() => []);
        return (Array.isArray(todas) ? todas : []).filter(v =>
            enRango(v.fechaHora, inicio, fin)
            && (!estado || v.estado === estado)
            && (!metodoPago || v.metodoPago === metodoPago));
    },
    comprasPorRango: async (inicio, fin) => {
        const todas = await comprasService.listar().catch(() => []);
        return (Array.isArray(todas) ? todas : []).filter(c =>
            enRango(c.fechaFactura || c.fechaRegistro, inicio, fin));
    },
    resumen: async (inicio, fin) => {
        const [ventas, compras, creditos, facturas] = await Promise.all([
            reportesService.ventasPorRango(inicio, fin),
            reportesService.comprasPorRango(inicio, fin),
            creditosService.listarTodos().catch(() => []),
            facturasService.listar().catch(() => [])
        ]);
        const totalVentas = ventas.filter(v => v.estado !== 'CANCELADA').reduce((s, v) => s + (Number(v.total) || 0), 0);
        const totalCompras = compras.reduce((s, c) => s + (Number(c.total) || 0), 0);
        const deudaCreditos = creditos.filter(c => c.estado === 'ACTIVO').reduce((s, c) => s + (Number(c.saldoPendiente) || 0), 0);
        const totalFacturado = facturas.filter(f => f.estado !== 'CANCELADA').reduce((s, f) => s + (Number(f.total) || 0), 0);
        return {
            ventas: ventas.length, totalVentas,
            compras: compras.length, totalCompras,
            creditosActivos: creditos.filter(c => c.estado === 'ACTIVO').length, deudaCreditos,
            facturas: facturas.filter(f => f.estado !== 'CANCELADA').length, totalFacturado,
            margen: totalVentas - totalCompras
        };
    }
};
