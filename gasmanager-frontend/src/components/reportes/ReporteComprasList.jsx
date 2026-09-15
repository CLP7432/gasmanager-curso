import React, {useState, useEffect} from "react";
import {reportesService, toCSV, downloadBlob} from "../../api/reportes/auth.js";
import {columna} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import CardBase from "../../kernel/components/CardBase.jsx";
import {BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer} from "recharts";

const dinero = (v) => `$${Number(v || 0).toLocaleString('es-MX', {minimumFractionDigits: 2})}`;
const hoyISO = () => new Date().toISOString().split('T')[0];
const hace30 = () => { const d = new Date(); d.setDate(d.getDate() - 30); return d.toISOString().split('T')[0]; };

const columnasCSV = [
    {label: 'Folio', key: 'folioFactura'},
    {label: 'Fecha', get: (c) => c.fechaFactura || c.fechaRegistro},
    {label: 'Proveedor', key: 'proveedorRazonSocial'},
    {label: 'Total', get: (c) => c.total}
];

const ReporteComprasList = () => {
    const [filtros, setFiltros] = useState({inicio: hace30(), fin: hoyISO()});
    const [compras, setCompras] = useState([]);
    const [loading, setLoading] = useState(true);

    const cargar = async () => {
        setLoading(true);
        try {
            setCompras(await reportesService.comprasPorRango(filtros.inicio, filtros.fin));
        } catch { setCompras([]); }
        setLoading(false);
    };

    useEffect(() => { cargar(); }, []);
    const buscar = (e) => { e.preventDefault(); cargar(); };

    const total = compras.reduce((s, c) => s + (Number(c.total) || 0), 0);
    const porDia = Object.values(compras.reduce((acc, c) => {
        const dia = String(c.fechaFactura || c.fechaRegistro || '').slice(0, 10);
        acc[dia] = acc[dia] || {dia, total: 0};
        acc[dia].total += Number(c.total) || 0;
        return acc;
    }, {})).sort((a, b) => a.dia.localeCompare(b.dia));

    const exportar = () => {
        downloadBlob(new Blob([toCSV(compras, columnasCSV)], {type: 'text/csv;charset=utf-8'}), `reporte_compras_${filtros.inicio}_${filtros.fin}.csv`);
    };

    const columnas = [
        columna('folioFactura', 'Folio'),
        columna('fechaFactura', 'Fecha', (c) => String(c.fechaFactura || c.fechaRegistro || '').slice(0, 10)),
        columna('proveedorRazonSocial', 'Proveedor'),
        columna('total', 'Total', (c) => <strong style={{color: '#b8860b'}}>{dinero(c.total)}</strong>)
    ];

    return (
        <div>
            <PageHeader titulo="Reporte de Compras" subtitulo={`Total: ${dinero(total)} en ${compras.length} facturas`} mostrarAccion={false} />
            <CardBase titulo="Filtros" style={{maxWidth: '900px', padding: '1rem', marginBottom: '15px'}}>
                <form onSubmit={buscar} className="row g-2 align-items-end">
                    <div className="col-md-4">
                        <label className="form-label">Desde</label>
                        <input type="date" className="form-control" value={filtros.inicio} onChange={(e) => setFiltros({...filtros, inicio: e.target.value})} />
                    </div>
                    <div className="col-md-4">
                        <label className="form-label">Hasta</label>
                        <input type="date" className="form-control" value={filtros.fin} onChange={(e) => setFiltros({...filtros, fin: e.target.value})} />
                    </div>
                    <div className="col-md-4 d-flex gap-1">
                        <button type="submit" className="btn btn-primary btn-sm">Buscar</button>
                        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={exportar}>CSV</button>
                    </div>
                </form>
            </CardBase>
            {porDia.length > 0 && (
                <CardBase titulo="Compras por día" style={{maxWidth: '900px', padding: '1rem', marginBottom: '15px'}}>
                    <ResponsiveContainer width="100%" height={220}>
                        <BarChart data={porDia}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="dia" tick={{fontSize: 11}} />
                            <YAxis tick={{fontSize: 11}} />
                            <Tooltip formatter={(v) => dinero(v)} />
                            <Bar dataKey="total" fill="#b8860b" name="Total" />
                        </BarChart>
                    </ResponsiveContainer>
                </CardBase>
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica columnas={columnas} datos={compras} />
            </EstadoCarga>
        </div>
    );
};
export default ReporteComprasList;
