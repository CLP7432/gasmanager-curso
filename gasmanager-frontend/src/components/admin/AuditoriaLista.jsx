import React, {useState} from "react";
import {auditoriaService} from "../../api/admin/auth.js";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import {columna, idCol} from "../../kernel/helpers/columnas.jsx";

const AuditoriaLista = () => {
    const [filtroUsuario, setFiltroUsuario] = useState('');
    const [fechaInicio, setFechaInicio] = useState('');
    const [fechaFin, setFechaFin] = useState('');
    const [auditorias, setAuditorias] = useState([]);
    const [loading, setLoading] = useState(false);

    const cargarAuditorias = async () => {
        setLoading(true);
        try {
            let data;
            if (filtroUsuario) {
                data = await auditoriaService.listarPorUsuario(filtroUsuario);
            } else if (fechaInicio && fechaFin) {
                data = await auditoriaService.listarPorRango(`${fechaInicio}:00`, `${fechaFin}:00`);
            } else {
                data = await auditoriaService.listarTodas();
            }
            setAuditorias(data);
        } catch (error) {
            console.error('Error al cargar auditorias: ', error);
        }
        setLoading(false);
    };

    const limpiarFiltros = () => {
        setFiltroUsuario('');
        setFechaInicio('');
        setFechaFin('');
        cargarAuditorias();
    };

    const getTipoBadge = (tipo) => {
        const colores = {
            CREAR: 'badge badge-success',
            ACTUALIZAR: 'badge badge-warning',
            ELIMINAR: 'badge badge-danger',
            LEER: 'badge badge-info',
            VALIDAR: 'badge badge-info'
        };
        return <span className={colores[tipo] || 'badge badge-secondary'}>{tipo}</span>;
    };

    const columnas = [
        idCol,
        columna('fechaHora', 'Fecha/Hora', (a) => new Date(a.fechaHora).toLocaleString()),
        columna('idUsuarioEjecutor', 'Usuario', (a) => a.idUsuarioEjecutor || 'Sistema'),
        columna('tipoAccion', 'Tipo', (a) => getTipoBadge(a.tipoAccion)),
        columna('moduloAfectado', 'Módulo'),
        columna('descripcion', 'Descripción'),
        columna('origen', 'Origen')
    ];

    return (
        <div>
            <h2>Registro de Auditoría</h2>
            <div className="card" style={{marginBottom: '20px'}}>
                <div style={{display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'flex-end'}}>
                    <div className="form-group" style={{marginBottom: '0'}}>
                        <label>ID Usuario</label>
                        <input type="number" value={filtroUsuario} onChange={(e) => setFiltroUsuario(e.target.value)}
                               placeholder="Filtrar por usuario" style={{width: '150px'}} />
                    </div>
                    <div className="form-group" style={{marginBottom: '0'}}>
                        <label>Fecha Inicio</label>
                        <input type="datetime-local" value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)}
                               style={{width: '200px'}} />
                    </div>
                    <div className="form-group" style={{marginBottom: '0'}}>
                        <label>Fecha Fin</label>
                        <input type="datetime-local" value={fechaFin} onChange={(e) => setFechaFin(e.target.value)}
                               style={{width: '200px'}} />
                    </div>
                    <button className="btn btn-primary" onClick={cargarAuditorias}>Filtrar</button>
                    <button className="btn btn-secondary" onClick={limpiarFiltros}>Limpiar</button>
                </div>
            </div>
            <div className="card">
                {loading ? (
                    <p>Cargando auditorías...</p>
                ) : (
                    <TablaDinamica columnas={columnas} datos={auditorias}/>
                )}
            </div>
        </div>
    );
};

export default AuditoriaLista;