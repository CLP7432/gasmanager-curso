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
    {label: 'Folio', key: 'folio'},
    {label: 'Fecha', key: 'fechaHora'},
    {label: 'Método', key: 'metodoPago'},
    {label: 'Estado', key: 'estado'},
    {label: 'Total', get: (v) => v.total}
];

const ReporteVentasList = () => {
    const [filtros, setFiltros] = useState({inicio: hace30(), fin: hoyISO(), estado: '', metodoPago: ''});
    const [ventas, setVentas] = useState([]);
    const [loading, setLoading] = useState(true);

    const cargar = async () => {
        setLoading(true);
        try {
            setVentas(await reportesService.ventasPorRango(filtros.inicio, filtros.fin, {estado: filtros.estado, metodoPago: filtros.metodoPago}));
        } catch { setVentas([]); }
        setLoading(false);
    };

    useEffect(() => { cargar(); }, []);
    const buscar = (e) => { e.preventDefault(); cargar(); };

    const total = ventas.filter(v => v.estado !== 'CANCELADA').reduce((s, v) => s + (Number(v.total) || 0), 0);
    const porDia = Object.values(ventas.filter(v => v.estado !== 'CANCELADA').reduce((acc, v) => {
        const dia = String(v.fechaHora || '').slice(0, 10);
        acc[dia] = acc[dia] || {dia, total: 0, n: 0};
        acc[dia].total += Number(v.total) || 0;
        acc[dia].n += 1;
        return acc;
    }, {})).sort((a, b) => a.dia.localeCompare(b.dia));

    const exportar = () => {
        downloadBlob(new Blob([toCSV(ventas, columnasCSV)], {type: 'text/csv;charset=utf-8'}), `reporte_ventas_${filtros.inicio}_${filtros.fin}.csv`);
    };

    const columnas = [
        columna('folio', 'Folio'),
        columna('fechaHora', 'Fecha', (v) => String(v.fechaHora || '').slice(0, 16).replace('T', ' ')),
        columna('metodoPago', 'Pago'),
        columna('estado', 'Estado'),
        columna('total', 'Total', (v) => <strong style={{color: '#198754'}}>{dinero(v.total)}</strong>)
    ];

    return (
        <div>
            <PageHeader titulo="Reporte de Ventas" subtitulo={`Total: ${dinero(total)} en ${ventas.length} tickets`} mostrarAccion={false} />
            <CardBase titulo="Filtros" style={{maxWidth: '900px', padding: '1rem', marginBottom: '15px'}}>
                <form onSubmit={buscar} className="row g-2 align-items-end">
                    <div className="col-md-3">
                        <label className="form-label">Desde</label>
                        <input type="date" className="form-control" value={filtros.inicio} onChange={(e) => setFiltros({...filtros, inicio: e.target.value})} />
                    </div>
                    <div className="col-md-3">
                        <label className="form-label">Hasta</label>
                        <input type="date" className="form-control" value={filtros.fin} onChange={(e) => setFiltros({...filtros, fin: e.target.value})} />
                    </div>
                    <div className="col-md-2">
                        <label className="form-label">Estado</label>
                        <select className="form-select" value={filtros.estado} onChange={(e) => setFiltros({...filtros, estado: e.target.value})}>
                            <option value="">Todos</option>
                            <option value="COMPLETADA">Completada</option>
                            <option value="CANCELADA">Cancelada</option>
                            <option value="PENDIENTE">Pendiente</option>
                        </select>
                    </div>
                    <div className="col-md-2">
                        <label className="form-label">Pago</label>
                        <select className="form-select" value={filtros.metodoPago} onChange={(e) => setFiltros({...filtros, metodoPago: e.target.value})}>
                            <option value="">Todos</option>
                            <option value="EFECTIVO">Efectivo</option>
                            <option value="TARJETA_CREDITO">T. Crédito</option>
                            <option value="TARJETA_DEBITO">T. Débito</option>
                            <option value="TRANSFERENCIA">Transferencia</option>
                            <option value="CREDITO">Crédito</option>
                        </select>
                    </div>
                    <div className="col-md-2 d-flex gap-1">
                        <button type="submit" className="btn btn-primary btn-sm">Buscar</button>
                        <button type="button" className="btn btn-outline-secondary btn-sm" onClick={exportar}>CSV</button>
                    </div>
                </form>
            </CardBase>
            {porDia.length > 0 && (
                <CardBase titulo="Ventas por día" style={{maxWidth: '900px', padding: '1rem', marginBottom: '15px'}}>
                    <ResponsiveContainer width="100%" height={220}>
                        <BarChart data={porDia}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="dia" tick={{fontSize: 11}} />
                            <YAxis tick={{fontSize: 11}} />
                            <Tooltip formatter={(v) => dinero(v)} />
                            <Bar dataKey="total" fill="#198754" name="Total" />
                        </BarChart>
                    </ResponsiveContainer>
                </CardBase>
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica columnas={columnas} datos={ventas} />
            </EstadoCarga>
        </div>
    );
};
export default ReporteVentasList;
