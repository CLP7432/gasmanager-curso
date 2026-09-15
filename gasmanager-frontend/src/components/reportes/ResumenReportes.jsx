import React, {useState, useEffect} from "react";
import {reportesService} from "../../api/reportes/auth.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import CardBase from "../../kernel/components/CardBase.jsx";

const dinero = (v) => `$${Number(v || 0).toLocaleString('es-MX', {minimumFractionDigits: 2})}`;
const hoyISO = () => new Date().toISOString().split('T')[0];
const hace30 = () => { const d = new Date(); d.setDate(d.getDate() - 30); return d.toISOString().split('T')[0]; };

const Tarjeta = ({titulo, valor, color}) => (
    <div className="card" style={{borderTop: `4px solid ${color}`, minWidth: '180px', flex: 1}}>
        <div className="card-body py-2">
            <small className="text-muted">{titulo}</small>
            <div className="fs-5 fw-bold" style={{color}}>{valor}</div>
        </div>
    </div>
);

const ResumenReportes = () => {
    const [filtros, setFiltros] = useState({inicio: hace30(), fin: hoyISO()});
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(true);

    const cargar = async () => {
        setLoading(true);
        try {
            setRes(await reportesService.resumen(filtros.inicio, filtros.fin));
        } catch { setRes(null); }
        setLoading(false);
    };

    useEffect(() => { cargar(); }, []);

    return (
        <div>
            <PageHeader titulo="Resumen general" subtitulo="Ventas, compras, créditos y facturación del periodo" mostrarAccion={false} />
            <CardBase titulo="Periodo" style={{maxWidth: '700px', padding: '1rem', marginBottom: '15px'}}>
                <form onSubmit={(e) => { e.preventDefault(); cargar(); }} className="row g-2 align-items-end">
                    <div className="col-md-5">
                        <label className="form-label">Desde</label>
                        <input type="date" className="form-control" value={filtros.inicio} onChange={(e) => setFiltros({...filtros, inicio: e.target.value})} />
                    </div>
                    <div className="col-md-5">
                        <label className="form-label">Hasta</label>
                        <input type="date" className="form-control" value={filtros.fin} onChange={(e) => setFiltros({...filtros, fin: e.target.value})} />
                    </div>
                    <div className="col-md-2">
                        <button type="submit" className="btn btn-primary btn-sm">Ver</button>
                    </div>
                </form>
            </CardBase>
            <EstadoCarga cargando={loading}>
                {res && (
                    <div className="d-flex gap-3 flex-wrap">
                        <Tarjeta titulo={`Ventas (${res.ventas})`} valor={dinero(res.totalVentas)} color="#198754" />
                        <Tarjeta titulo={`Compras (${res.compras})`} valor={dinero(res.totalCompras)} color="#b8860b" />
                        <Tarjeta titulo="Margen" valor={dinero(res.margen)} color={res.margen >= 0 ? '#0d6efd' : '#dc3545'} />
                        <Tarjeta titulo={`Créditos activos (${res.creditosActivos})`} valor={dinero(res.deudaCreditos)} color="#6f42c1" />
                        <Tarjeta titulo={`Facturas (${res.facturas})`} valor={dinero(res.totalFacturado)} color="#0dcaf0" />
                    </div>
                )}
            </EstadoCarga>
        </div>
    );
};
export default ResumenReportes;
