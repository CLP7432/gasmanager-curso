import React, {useState, useEffect} from "react";
import {useParams, useNavigate} from "react-router-dom";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {notasCreditoService} from "../../api/clients/auth.js";
import {combustiblesService, aceitesService} from "../../api/inventarios/auth.js";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import EditorItems from "../../kernel/components/EditorItems.jsx";
import {campoTexto, campoFecha} from "../../kernel/helpers/campos.jsx";

const dinero = (v) => Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2});

const estadoBadge = (estado) => {
    const colores = {ACTIVA: 'badge-success', AGOTADA: 'badge-secondary', BLOQUEADA: 'badge-warning', VENCIDA: 'badge-danger', PAGADA: 'badge-info'};
    return <span className={`badge ${colores[estado] || 'badge-secondary'}`}>{estado}</span>;
};

const NotaDetalle = () => {
    const {id} = useParams();
    const navigate = useNavigate();
    const {isAdmin} = useAuth();
    const [nota, setNota] = useState(null);
    const [loading, setLoading] = useState(true);
    const [mostrarForm, setMostrarForm] = useState(false);
    const [objeto, setObjeto] = useState({vehiculo: '', conductor: '', fechaCarga: ''});
    const [items, setItems] = useState([]);
    const [combustibles, setCombustibles] = useState([]);
    const [aceites, setAceites] = useState([]);

    useEffect(() => {
        Promise.all([
            notasCreditoService.obtenerPorId(id),
            combustiblesService.listarActivos(),
            aceitesService.listarActivos()
        ])
            .then(([n, c, a]) => {
                setNota(n);
                setCombustibles(c);
                setAceites(a);
            })
            .catch(() => alert('Error al cargar la nota'))
            .finally(() => setLoading(false));
    }, [id]);

    const abrirEditar = () => {
        setItems((nota.items || []).map(i => i.tipo === 'COMBUSTIBLE'
            ? {tipo: i.tipo, producto: i.producto, monto: i.subtotal || (i.cantidad * i.precioUnitario), cantidad: '', precioUnitario: i.precioUnitario}
            : {tipo: i.tipo, producto: i.producto, cantidad: i.cantidad, monto: '', precioUnitario: i.precioUnitario}));
        setObjeto({vehiculo: nota.vehiculo || '', conductor: nota.conductor || '', fechaCarga: nota.fechaCarga || new Date().toISOString().slice(0, 10)});
        setMostrarForm(true);
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        setObjeto(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (items.length === 0) return alert('Debe registrar al menos un producto cargado');
        try {
            const itemsNormalizados = items.map(i => {
                if (i.tipo === 'COMBUSTIBLE') {
                    const precio = parseFloat(i.precioUnitario) || 0;
                    const monto = parseFloat(i.monto) || 0;
                    return {...i, cantidad: precio > 0 ? +(monto / precio).toFixed(3) : 0, subtotal: monto};
                }
                return {...i, subtotal: (parseFloat(i.cantidad) || 0) * (parseFloat(i.precioUnitario) || 0)};
            });
            await notasCreditoService.actualizar(id, {...objeto, creditoId: nota.creditoId, items: itemsNormalizados});
            alert('Nota actualizada');
            setMostrarForm(false);
            setNota(await notasCreditoService.obtenerPorId(id));
        } catch (error) {
            alert(error.response?.data?.message || 'Error al guardar');
        }
    };

    if (loading) return <EstadoCarga cargando={true}/>;
    if (!nota) return <p className="text-muted">Nota no encontrada.</p>;

    const campo3 = (label, valor) => (
        <div className="col-6 col-md-4">
            <small className="text-muted">{label}</small>
            <div>{valor || '-'}</div>
        </div>
    );

    const itemsTabla = (items) => (
        <div style={{fontSize: '0.85em'}}>
            <div className="fw-bold d-flex gap-2" style={{borderBottom: '1px solid #dee2e6', marginBottom: '4px', paddingBottom: '2px'}}>
                <span style={{flex: 2}}>Producto</span>
                <span style={{flex: 1}}>Cant.</span>
                <span style={{flex: 1}}>P. Unit.</span>
                <span style={{flex: 1, textAlign: 'right'}}>Subtotal</span>
            </div>
            {items.map(i => (
                <div key={i.id} className="d-flex gap-2" style={{borderBottom: '1px solid #f1f3f5', padding: '2px 0'}}>
                    <span style={{flex: 2}}>{i.producto}</span>
                    <span style={{flex: 1}}>{i.cantidad} {i.unidad}</span>
                    <span style={{flex: 1}}>${Number(i.precioUnitario).toFixed(2)}</span>
                    <span style={{flex: 1, textAlign: 'right'}}><strong>${Number(i.subtotal).toFixed(2)}</strong></span>
                </div>
            ))}
        </div>
    );

    return (
        <div>
            <button className="btn btn-secondary" style={{marginBottom: '20px'}} onClick={() => navigate('/clientes/notas-credito')}>
                ← Volver a Notas de Crédito
            </button>
            <div className="card shadow-sm" style={{maxWidth: '760px'}}>
                <div className="card-header d-flex justify-content-between align-items-center" style={{background: '#0f172a', color: '#fff'}}>
                    <strong>{nota.numero}</strong>
                    {estadoBadge(nota.estado)}
                </div>
                <div className="card-body">
                    {!mostrarForm && (
                        <div>
                            <div className="row g-2">
                                {campo3('Cliente', nota.clienteNombre)}
                                {campo3('Crédito', nota.creditoFolio)}
                                {campo3('Vehículo', nota.vehiculo)}
                                {campo3('Conductor', nota.conductor)}
                                {campo3('Fecha de carga', nota.fechaCarga)}
                                <div className="col-6 col-md-4">
                                    <small className="text-muted">Total</small>
                                    <div className="fw-bold" style={{color: '#198754'}}>${dinero(nota.saldo)}</div>
                                </div>
                            </div>
                            <hr style={{margin: '12px 0'}}/>
                            <h6 className="fw-bold">Productos</h6>
                            {nota.items && nota.items.length > 0
                                ? itemsTabla(nota.items)
                                : <span className="text-muted">Sin productos</span>}
                            {isAdmin && nota.estado !== 'PAGADA' && (
                                <div style={{marginTop: '15px'}}>
                                    <button className="btn btn-primary btn-sm" onClick={abrirEditar}>Editar nota</button>
                                </div>
                            )}
                        </div>
                    )}
                    {mostrarForm && (
                        <FormularioBase
                            titulo="Editar Nota"
                            campos={[
                                campoTexto('vehiculo', 'Vehículo'),
                                campoTexto('conductor', 'Conductor'),
                                campoFecha('fechaCarga', 'Fecha de Carga')
                            ]}
                            objeto={objeto}
                            handleChange={handleChange}
                            handleSubmit={handleSubmit}
                            cerrar={() => setMostrarForm(false)}
                            renderExtra={() => (
                                <div style={{marginTop: '15px'}}>
                                    <h5>Productos cargados</h5>
                                    <EditorItems items={items} onChange={setItems}
                                                 combustibles={combustibles} aceites={aceites}/>
                                </div>
                            )}
                        />
                    )}
                </div>
            </div>
        </div>
    );
};

export default NotaDetalle;