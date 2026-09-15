import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import {notasCreditoService, creditosService} from "../../api/clients/auth.js";
import {facturasService} from "../../api/facturacion/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import {columna} from "../../kernel/helpers/columnas.jsx";

const PagosList = () => {
    const navigate = useNavigate();
    const {datos: abonos, loading} = useLista(notasCreditoService, 'listarTodosAbonos');
    const {datos: facturas} = useLista(facturasService, 'listar');
    const [clienteFiltro, setClienteFiltro] = useState('');
    const {datos: creditos} = useLista(creditosService, 'listarTodos');

    const creditoDe = (creditoId) => creditos.find(x => x.id === creditoId) || null;

    const clientePorCredito = (creditoId) => {
        const c = creditoDe(creditoId);
        return c?.clienteNombre || c?.cliente?.razonSocial || c?.cliente?.nombre || '';
    };

    const clienteIdDe = (creditoId) => creditoDe(creditoId)?.clienteId || null;

    // Folios de notas incluidos en el abono (los guarda liquidar como "Notas: N1, N2")
    const foliosDelAbono = (a) => {
        const m = String(a.notas || '').match(/NOTA-[\w-]+/g);
        return m || [];
    };

    // Factura(s) que amparan esas notas
    const facturasDelAbono = (a) => {
        const folios = foliosDelAbono(a);
        if (folios.length === 0) return [];
        return (facturas || []).filter(f =>
            f.estado !== 'CANCELADA' && (f.conceptos || []).some(c =>
                c.origen === 'NOTA_CREDITO' && folios.includes(c.origenFolio)));
    };

    const clientesFiltro = [...new Map((abonos || []).map(a => {
        const id = clienteIdDe(a.creditoId);
        return [id, {id, nombre: clientePorCredito(a.creditoId) || (id ? `Cliente #${id}` : 'Sin cliente')}];
    }).filter(([id]) => id != null)).values()];

    const data = (abonos || [])
        .filter(a => !clienteFiltro || String(clienteIdDe(a.creditoId)) === String(clienteFiltro))
        .map(a => ({
            ...a,
            cliente: clientePorCredito(a.creditoId),
            facturas: facturasDelAbono(a)
        }));

    const columnas = [
        columna('folioAbono', 'Folio'),
        columna('creditoFolio', 'Crédito'),
        columna('cliente', 'Cliente'),
        columna('fechaAbono', 'Fecha'),
        columna('monto', 'Monto', (v) => `$${Number(v.monto).toLocaleString('es-MX', {minimumFractionDigits: 2})}`),
        columna('metodoPago', 'Método'),
        columna('facturas', 'Factura', (v) => v.facturas.length === 0 ? '—' : v.facturas.map(f => (
            <span key={f.id} className="badge bg-info me-1">{f.folio}</span>
        )))
    ];

    return (
        <div>
            <button className="btn btn-secondary btn-sm mb-2" onClick={() => navigate('/clientes')}>
                ← Volver a Clientes
            </button>
            <PageHeader
                titulo="Pagos y Abonos"
                subtitulo="Historial de pagos (incluye liquidaciones de notas). Los pagos se registran en Notas de Crédito."
                etiquetaAccion="+ Nuevo"
                onAccion={() => navigate('/clientes/notas-credito')}
            />
            <EstadoCarga cargando={loading}>
                <div className="card mb-2" style={{maxWidth: '500px'}}>
                    <div className="card-body py-2">
                        <label className="form-label small mb-1">Filtrar por cliente de crédito</label>
                        <select className="form-select form-select-sm" value={clienteFiltro} onChange={(e) => setClienteFiltro(e.target.value)}>
                            <option value="">Todos</option>
                            {clientesFiltro.map(c => (
                                <option key={c.id} value={c.id}>#{c.id} — {c.nombre}</option>
                            ))}
                        </select>
                    </div>
                </div>
                <TablaDinamica columnas={columnas} datos={data}/>
            </EstadoCarga>
        </div>
    );
};

export default PagosList;