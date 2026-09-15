import React, {useState} from "react";
import {useNavigate} from "react-router-dom";
import {facturasService, clientesFiscalesService} from "../../api/facturacion/auth.js";
import {clientesService, notasCreditoService} from "../../api/clients/auth.js";import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import CardBase from "../../kernel/components/CardBase.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const NotasPorFacturarList = () => {
    const navigate = useNavigate();
    const {datos: clientes, loading: cargandoClientes} = useLista(clientesService, 'listar');
    const {datos: facturas} = useLista(facturasService, 'listar');
    const {datos: todasNotas} = useLista(notasCreditoService, 'listar');
    const [clienteId, setClienteId] = useState('');
    const [disponibles, setDisponibles] = useState(null);
    const [cargando, setCargando] = useState(false);
    const [seleccionadas, setSeleccionadas] = useState([]);
    const [sinFiscal, setSinFiscal] = useState(false);
    const [sinLiquidar, setSinLiquidar] = useState(0);

    const clienteSel = (clientes || []).find(c => String(c.id) === String(clienteId)) || null;

    // Solo clientes con notas liquidadas aún no facturadas; el ya facturado sale del select
    const facturados = new Set();
    (facturas || []).filter(f => f.estado !== 'CANCELADA').forEach(f => {
        (f.conceptos || []).forEach(c => {
            if (c.origen === 'NOTA_CREDITO' && c.origenFolio) facturados.add(c.origenFolio);
        });
    });
    // Clientes con algo pendiente: notas liquidadas sin facturar O notas sin
    // liquidar (esas muestran el aviso de pago). Solo sale quien ya facturó todo.
    const elegibles = new Set(
        (todasNotas || [])
            .filter(n => !(n.estado === 'PAGADA' && facturados.has(n.numero)))
            .map(n => String(n.clienteId))
    );
    const clientesOpciones = (clientes || []).filter(c =>
        c.activo !== false && (elegibles.has(String(c.id)) || String(c.id) === String(clienteId)));

    const consultar = async (id) => {
        setClienteId(id);
        setDisponibles(null);
        setSeleccionadas([]);
        setSinFiscal(false);
        setSinLiquidar(0);
        if (!id) return;
        setCargando(true);
        try {
            const [d, f, n] = await Promise.all([
                facturasService.disponibles(id),
                clientesFiscalesService.obtenerPorCliente(id).catch(() => null),
                notasCreditoService.listarPorCliente(id).catch(() => [])
            ]);
            const pendientes = (d.notas || []).filter(n => !n.yaFacturada);
            setDisponibles({...d, notas: pendientes});
            setSeleccionadas(pendientes.map((_, i) => i));
            setSinFiscal(!f);
            setSinLiquidar((Array.isArray(n) ? n : []).filter(x => x.estado !== 'PAGADA').length);
        } catch {
            setDisponibles({clienteId: id, clienteNombre: null, notas: []});
        }
        setCargando(false);
    };

    const toggle = (i) => setSeleccionadas(prev => prev.includes(i) ? prev.filter(x => x !== i) : [...prev, i]);

    const totalSel = seleccionadas.reduce((s, i) => {
        const n = disponibles?.notas?.[i];
        return s + (Number(n?.cantidad) || 0) * (Number(n?.valorUnitario) || 0);
    }, 0);

    const continuar = () => {
        const elegidas = seleccionadas.map(i => disponibles.notas[i]);
        // Agrupar líneas por folio de nota para reutilizar el flujo de prefactura
        const porFolio = {};
        elegidas.forEach(it => {
            if (!porFolio[it.origenFolio]) porFolio[it.origenFolio] = [];
            porFolio[it.origenFolio].push({
                tipo: it.esCombustible ? 'COMBUSTIBLE' : 'ACEITE',
                producto: it.descripcion,
                cantidad: it.cantidad,
                unidad: it.unidad,
                precioUnitario: it.valorUnitario
            });
        });
        const notas = Object.entries(porFolio).map(([numero, items]) => {
            return {numero, items};
        });
        navigate('/facturacion/facturas', {
            state: {facturarNotas: notas, clienteId: Number(clienteId), clienteNombre: disponibles?.clienteNombre}
        });
    };

    return (
        <div>
            <PageHeader titulo="Notas por facturar" subtitulo="Consumos a crédito pendientes de factura" mostrarAccion={false} />
            <button className="btn btn-secondary btn-sm mb-2" onClick={() => navigate('/facturacion')}>
                ← Volver a Facturación
            </button>
            <CardBase titulo="Cliente" style={{maxWidth: '760px', padding: '1rem', marginBottom: '15px'}}>
                <label className="form-label">Selecciona el cliente</label>
                <select className="form-select" value={clienteId} onChange={(e) => consultar(e.target.value)} disabled={cargandoClientes}>
                    <option value="">Seleccionar cliente...</option>
                    {clientesOpciones.map(c => (
                        <option key={c.id} value={c.id}>
                            #{c.id} — {c.codigoCliente || 's/c'} — {c.razonSocial || c.nombre || c.nombreComercial || 'sin nombre'}
                        </option>
                    ))}
                </select>
                {clienteSel && (
                    <small className="text-muted d-block mt-2">
                        {clienteSel.razonSocial || clienteSel.nombre} · RFC: {clienteSel.rfc || '-'}
                    </small>
                )}
                {sinFiscal && clienteId && (
                    <div className="alert alert-warning py-2 mt-3 mb-0">
                        Este cliente aún no tiene registro fiscal.{' '}
                        <button className="btn btn-sm btn-warning ms-2" onClick={() => navigate('/facturacion/clientes-fiscales')}>
                            Registrar datos fiscales
                        </button>
                    </div>
                )}
            </CardBase>
            <EstadoCarga cargando={cargando}>
                {!clienteId ? (
                    <CardBase titulo="Selecciona un cliente">
                        <p className="mb-0">Elige un cliente arriba para ver sus notas pendientes de factura.</p>
                    </CardBase>
                ) : !disponibles || disponibles.notas.length === 0 ? (
                    <CardBase titulo="Sin pendientes">
                        {sinLiquidar > 0 ? (
                            <div>
                                <p className="mb-2">Este cliente tiene <strong>{sinLiquidar} nota(s) sin liquidar</strong>: no pueden facturarse hasta que se realice el pago en <strong>Clientes → Notas de Crédito</strong>.</p>
                                <button className="btn btn-sm btn-primary" onClick={() => navigate('/clientes/notas-credito', {state: {clienteId: Number(clienteId)}})}>
                                    Ir a liquidar notas
                                </button>
                            </div>
                        ) : (
                            <p className="mb-0">Este cliente no tiene notas pendientes de factura.</p>
                        )}
                    </CardBase>
                ) : (
                    <CardBase titulo={`Notas pendientes · Total seleccionado: ${dinero(totalSel)}`} style={{maxWidth: '900px', padding: '1rem'}}>
                        <div style={{display: 'flex', flexDirection: 'column', gap: '6px', marginBottom: '12px'}}>
                            {disponibles.notas.map((n, i) => (
                                <div key={i} className="d-flex align-items-center gap-2 p-2"
                                     style={{border: seleccionadas.includes(i) ? '1px solid #0d6efd' : '1px solid #e9ecef', borderRadius: '6px', background: seleccionadas.includes(i) ? '#f0f7ff' : '#fff'}}>
                                    <input type="checkbox" className="form-check-input" style={{marginTop: 0}}
                                           checked={seleccionadas.includes(i)} onChange={() => toggle(i)} />
                                    <div className="flex-grow-1">
                                        <div><strong>{n.origenFolio}</strong> <small className="text-muted">{n.descripcion}</small></div>
                                        <div className="text-muted" style={{fontSize: '0.85em'}}>
                                            {n.cantidad} {n.unidad} x ${n.valorUnitario} {n.esCombustible ? '· combustible' : '· aceite'}
                                        </div>
                                    </div>
                                    <div className="fw-bold" style={{color: '#198754'}}>{dinero((Number(n.cantidad) || 0) * (Number(n.valorUnitario) || 0))}</div>
                                </div>
                            ))}
                        </div>
                        <div className="d-flex justify-content-end">
                            <button className="btn btn-primary" disabled={seleccionadas.length === 0 || sinFiscal} onClick={continuar}>
                                Continuar a facturar
                            </button>
                        </div>
                        {sinFiscal && <small className="text-muted">Registra primero sus datos fiscales para continuar.</small>}
                    </CardBase>
                )}
            </EstadoCarga>
        </div>
    );
};
export default NotasPorFacturarList;
