import React, {useState} from "react";
import {campoFecha, campoTextarea} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {nominasService} from "../../api/nomina/auth.js";
import {idCol, columna} from "../../kernel/helpers/columnas.jsx";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const dinero = (v) => `$${Number(v || 0).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const nominaInicial = {
    periodoInicio: '',
    periodoFin: '',
    fechaPago: '',
    observaciones: ''
};

const estadoBadge = (estado) => {
    const colores = {PROCESADA: 'badge-info', PAGADA: 'badge-success', CANCELADA: 'badge-danger'};
    return <span className={`badge ${colores[estado] || 'badge-secondary'}`}>{estado}</span>;
};

const NominasList = () => {

    const {isAdmin} = useAuth();
    const [estado, setEstado] = useState('');
    const [detalle, setDetalle] = useState(null);
    const {datos: nominas, loading, cargar} = useLista(nominasService, 'listar');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleChange, handleSubmit, cerrar} =
        useFormulario(nominasService, nominaInicial, 'Nómina procesada', cargar,
            async (form) => nominasService.procesar(form));

    const camposNomina = [
        campoFecha('periodoInicio', 'Periodo Inicio', {required: true}),
        campoFecha('periodoFin', 'Periodo Fin', {required: true}),
        campoFecha('fechaPago', 'Fecha de Pago'),
        campoTextarea('observaciones', 'Observaciones', {colsClase: 'col-md-6', rows: 3})
    ];

    const visibles = estado === '' ? nominas : nominas.filter(n => n.estado === estado);

    const handlePagar = async (nomina) => {
        if (window.confirm(`¿Marcar como pagada la nómina ${nomina.folioNomina}?`)) {
            try {
                await nominasService.marcarPagada(nomina.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al marcar la nómina como pagada');
            }
        }
    };

    const handleCancelar = async (nomina) => {
        const motivo = window.prompt(`Motivo de cancelación de ${nomina.folioNomina}:`);
        if (motivo === null) return;
        try {
            await nominasService.cancelar(nomina.id, motivo);
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al cancelar la nómina');
        }
    };

    const columnas = [
        idCol,
        columna('folioNomina', 'Folio'),
        columna('periodo', 'Periodo', (n) => `${n.periodoInicio} → ${n.periodoFin}`),
        columna('fechaPago', 'Fecha de Pago'),
        columna('totalEmpleados', 'Empleados'),
        columna('totalSueldos', 'Sueldos', (n) => dinero(n.totalSueldos)),
        columna('totalDeducciones', 'Deducciones', (n) => dinero(n.totalDeducciones)),
        columna('totalNeto', 'Neto', (n) => <strong style={{color: '#198754'}}>{dinero(n.totalNeto)}</strong>),
        columna('estado', 'Estado', (n) => estadoBadge(n.estado))
    ];

    const columnasDetalle = [
        columna('empleadoCodigo', 'Código'),
        columna('empleadoNombre', 'Empleado'),
        columna('diasTrabajados', 'Días'),
        columna('sueldoBase', 'Sueldo', (d) => dinero(d.sueldoBase)),
        columna('horasExtrasMonto', 'H. Extra', (d) => dinero(d.horasExtrasMonto)),
        columna('bonos', 'Bonos', (d) => dinero(d.bonos)),
        columna('sobrantesMonto', 'Sobrantes', (d) => dinero(d.sobrantesMonto)),
        columna('totalGravado', 'Gravado', (d) => dinero(d.totalGravado)),
        columna('isr', 'ISR', (d) => dinero(d.isr)),
        columna('totalDeducciones', 'Deducciones', (d) => dinero(d.totalDeducciones)),
        columna('netoPagar', 'Neto', (d) => <strong>{dinero(d.netoPagar)}</strong>)
    ];

    return (
        <div>
            <PageHeader
                titulo="Nóminas"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Procesar Nómina"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Nómina"
                    editando={!!editandoId}
                    campos={camposNomina}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                    compacto
                />
            )}

            <div className="mb-3" style={{maxWidth: '280px'}}>
                <select
                    className="form-control"
                    value={estado}
                    onChange={(e) => setEstado(e.target.value)}
                >
                    <option value="">Todas las nóminas</option>
                    <option value="PROCESADA">Procesadas</option>
                    <option value="PAGADA">Pagadas</option>
                    <option value="CANCELADA">Canceladas</option>
                </select>
            </div>

            {detalle && (
                <div className="card mb-3">
                    <div className="card-header d-flex justify-content-between align-items-center" style={{background: '#0f172a', color: '#fff'}}>
                        <strong>{detalle.folioNomina} — {detalle.periodoInicio} a {detalle.periodoFin}</strong>
                        <button className="btn btn-sm btn-secondary" onClick={() => setDetalle(null)}>Cerrar</button>
                    </div>
                    <div className="card-body">
                        <div className="row g-2 mb-3" style={{fontSize: '0.9em'}}>
                            <div className="col-6 col-md-3"><small className="text-muted">Empleados</small><div>{detalle.totalEmpleados}</div></div>
                            <div className="col-6 col-md-3"><small className="text-muted">Sueldos</small><div>{dinero(detalle.totalSueldos)}</div></div>
                            <div className="col-6 col-md-3"><small className="text-muted">Horas extra</small><div>{dinero(detalle.totalHorasExtras)}</div></div>
                            <div className="col-6 col-md-3"><small className="text-muted">Bonos</small><div>{dinero(detalle.totalBonos)}</div></div>
                            <div className="col-6 col-md-3"><small className="text-muted">Deducciones</small><div>{dinero(detalle.totalDeducciones)}</div></div>
                            <div className="col-6 col-md-3"><small className="text-muted">Impuestos (ISR)</small><div>{dinero(detalle.totalImpuestos)}</div></div>
                            <div className="col-6 col-md-3"><small className="text-muted">Estado</small><div>{estadoBadge(detalle.estado)}</div></div>
                            <div className="col-6 col-md-3"><small className="text-muted">Neto a pagar</small><div className="fw-bold" style={{color: '#198754'}}>{dinero(detalle.totalNeto)}</div></div>
                        </div>
                        {detalle.observaciones && <p className="text-muted" style={{fontSize: '0.85em'}}>📝 {detalle.observaciones}</p>}
                        <TablaDinamica columnas={columnasDetalle} datos={detalle.detalles} />
                    </div>
                </div>
            )}

            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={visibles}
                    acciones={(nomina) => (
                        <Acciones
                            fila={nomina}
                            onExtra={() => setDetalle(nomina)}
                            etiquetaExtra="Detalle"
                            onToggle={nomina.estado === 'PROCESADA' ? handlePagar : undefined}
                            etiquetaToggle="Marcar Pagada"
                            onEliminar={nomina.estado !== 'PAGADA' ? handleCancelar : undefined}
                            etiquetaEliminar="Cancelar"
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default NominasList;