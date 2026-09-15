import React, {useState} from "react";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {dispensariosService} from "../../api/ventas/auth.js";
import {combustiblesService} from "../../api/inventarios/auth.js";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const MANGUERAS_FIJAS = [
    {id: null, cara: 'A', codigo: 'A1', nombre: 'Manguera A1', tipoCombustible: '', combustibleId: null},
    {id: null, cara: 'A', codigo: 'A2', nombre: 'Manguera A2', tipoCombustible: '', combustibleId: null},
    {id: null, cara: 'B', codigo: 'B1', nombre: 'Manguera B1', tipoCombustible: '', combustibleId: null},
    {id: null, cara: 'B', codigo: 'B2', nombre: 'Manguera B2', tipoCombustible: '', combustibleId: null}
];

const mangueraTipo = (dispensario, codigo) => {
    for (const cara of dispensario.caras || []) {
        const manguera = (cara.mangueras || []).find(m => m.codigo === codigo);
        if (manguera) return manguera.tipoCombustible;
    }
    return '';
};

const DispensariosLista = () => {
    const {isAdmin} = useAuth();
    const {datos: dispensarios, loading, cargar} = useLista(dispensariosService, 'listarCompletos');
    const {datos: combustibles} = useLista(combustiblesService, 'listarActivos');

    const [mostrarForm, setMostrarForm] = useState(false);
    const [editandoId, setEditandoId] = useState(null);
    const [formData, setFormData] = useState({numero: '', nombre: '', ubicacion: ''});
    const [mangueras, setMangueras] = useState(MANGUERAS_FIJAS.map(m => ({...m})));

    const handleNuevo = () => {
        setEditandoId(null);
        setFormData({numero: '', nombre: '', ubicacion: ''});
        setMangueras(MANGUERAS_FIJAS.map(m => ({...m})));
        setMostrarForm(true);
    };

    const handleEditar = async (dispensario) => {
        const completo = await dispensariosService.obtenerCompleto(dispensario.id);
        setEditandoId(completo.id);
        setFormData({numero: completo.numero, nombre: completo.nombre, ubicacion: completo.ubicacion || ''});
        const nuevas = MANGUERAS_FIJAS.map(m => ({...m}));
        for (const cara of completo.caras || []) {
            for (const manguera of cara.mangueras || []) {
                const idx = nuevas.findIndex(m => m.codigo === manguera.codigo);
                if (idx !== -1) {
                    nuevas[idx] = {...nuevas[idx], id: manguera.id,
                        tipoCombustible: manguera.tipoCombustible, combustibleId: manguera.combustibleId};
                }
            }
        }
        setMangueras(nuevas);
        setMostrarForm(true);
    };

    const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

    const handleMangueraCombustible = (index, combustibleId) => {
        const combustible = combustibles.find(c => c.id === Number(combustibleId));
        const nuevas = [...mangueras];
        nuevas[index] = {...nuevas[index], combustibleId: Number(combustibleId),
            tipoCombustible: combustible ? combustible.tipo : ''};
        setMangueras(nuevas);
    };
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.numero.trim() || !formData.nombre.trim()) { alert('Número y nombre son obligatorios'); return; }
        if (!mangueras.some(m => m.tipoCombustible)) { alert('Asigna combustible a al menos una manguera'); return; }
        const carasMap = new Map();
        for (const m of mangueras) {
            if (!carasMap.has(m.cara)) carasMap.set(m.cara, {codigo: m.cara, nombre: `Cara ${m.cara}`, activo: true, mangueras: []});
            if (m.tipoCombustible) {
                carasMap.get(m.cara).mangueras.push({id: m.id, codigo: m.codigo, nombre: m.nombre,
                    tipoCombustible: m.tipoCombustible, combustibleId: m.combustibleId, lecturaActual: 0, activo: true});
            }
        }
        const payload = {
            numero: formData.numero.trim(), nombre: formData.nombre.trim(),
            ubicacion: formData.ubicacion.trim(), activo: true, caras: Array.from(carasMap.values())
        };
        try {
            if (editandoId) await dispensariosService.actualizarCompleto(editandoId, payload);
            else await dispensariosService.crearCompleto(payload);
            alert('Dispensario guardado');
            setMostrarForm(false);
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al guardar el dispensario');
        }
    };

    const handleToggleActivo = async (dispensario) => {
        if (window.confirm('¿Cambiar el estado del dispensario?')) {
            try {
                await dispensariosService.cambiarActivo(dispensario.id, !dispensario.activo);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al cambiar el estado');
            }
        }
    };

    const columnas = [
        idCol,
        columna('numero', 'Número'),
        columna('nombre', 'Nombre'),
        columna('ubicacion', 'Ubicación'),
        columna('A1', 'Cara A1', (d) => mangueraTipo(d, 'A1') || '-'),
        columna('A2', 'Cara A2', (d) => mangueraTipo(d, 'A2') || '-'),
        columna('B1', 'Cara B1', (d) => mangueraTipo(d, 'B1') || '-'),
        columna('B2', 'Cara B2', (d) => mangueraTipo(d, 'B2') || '-'),
        estadoCol((d) => d.activo)
    ];

    const renderMangueras = (cara) => mangueras.filter(m => m.cara === cara).map((m) => {
        const idx = mangueras.findIndex(x => x.codigo === m.codigo);
        return (
            <div key={m.codigo} className="col-md-6">
                <label className="form-label">{m.nombre}</label>
                <select className="form-select" value={m.combustibleId || ''}
                        onChange={(e) => handleMangueraCombustible(idx, e.target.value)}>
                    <option value="">❌ Sin combustible</option>
                    {combustibles.map(c => (
                        <option key={c.id} value={c.id}>{c.nombre} ({c.tipo}) — {c.precioActual}/L</option>
                    ))}
                </select>
            </div>
        );
    });
    return (
        <div>
            <PageHeader
                titulo="Catálogo de Dispensarios"
                subtitulo="Islas: 2 caras (A/B) × 2 mangueras"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Dispensario"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <div className="card" style={{maxWidth: '1000px', padding: '1rem'}}>
                    <h4>{editandoId ? 'Editar' : 'Nuevo'} Dispensario</h4>
                    <form onSubmit={handleSubmit}>
                        <div className="row g-3">
                            <div className="col-md-3">
                                <label className="form-label">Número</label>
                                <input className="form-control" name="numero" value={formData.numero} onChange={handleChange}/>
                            </div>
                            <div className="col-md-4">
                                <label className="form-label">Nombre</label>
                                <input className="form-control" name="nombre" value={formData.nombre} onChange={handleChange}/>
                            </div>
                            <div className="col-md-5">
                                <label className="form-label">Ubicación</label>
                                <input className="form-control" name="ubicacion" value={formData.ubicacion} onChange={handleChange}/>
                            </div>
                        </div>
                        <div className="row g-3 mt-1">
                            <div className="col-md-6" style={{background: '#e8f4f8', borderRadius: '8px', padding: '1rem'}}>
                                <strong>Cara A</strong>
                                <div className="row g-2 mt-1">{renderMangueras('A')}</div>
                            </div>
                            <div className="col-md-6" style={{background: '#f0e6f5', borderRadius: '8px', padding: '1rem'}}>
                                <strong>Cara B</strong>
                                <div className="row g-2 mt-1">{renderMangueras('B')}</div>
                            </div>
                        </div>
                        <div className="d-flex gap-2 mt-3">
                            <button type="submit" className="btn btn-primary">Guardar</button>
                            <button type="button" className="btn btn-secondary" onClick={() => setMostrarForm(false)}>Cancelar</button>
                        </div>
                    </form>
                </div>
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={dispensarios}
                    acciones={(d) => (
                        <Acciones fila={d} onEditar={handleEditar}
                                  onToggle={handleToggleActivo}
                                  etiquetaToggle={d.activo ? 'Desactivar' : 'Activar'}/>
                    )}
                />
            </EstadoCarga>
        </div>
    );
};
export default DispensariosLista;