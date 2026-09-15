import React, {useState} from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {permisoService, rolService} from "../../api/admin/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import {campoTexto} from "../../kernel/helpers/campos.jsx";

const rolInicial = {
    nombreRol: '',
    descripcion: ''
};
const camposRol = [
    campoTexto('nombreRol', 'Nombre del Rol', {required: true}),
    campoTexto('descripcion', 'Descripción')
];

const RolesLista = () => {
    const [permisosSeleccionados, setPermisosSeleccionados] = useState([]);
    const {isAdmin} = useAuth();
    const {datos: roles, loading, cargar} = useLista(rolService, 'listar');
    const {datos: permisos} = useLista(permisoService, 'listar');

    const togglePermiso = (id) => {
        setPermisosSeleccionados(prev =>
            prev.includes(id) ? prev.filter(p => p !== id) : [...prev, id]);
    };

    const guardarRol = (objeto, editandoId) => {
        const rolData = {
            ...objeto,
            permisos: permisosSeleccionados.map(id => ({id}))
        };
        if (editandoId) {
            return rolService.actualizar(editandoId, rolData);
        }
        return rolService.crear(rolData);
    };

    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar: handleEditarBase, handleChange, handleSubmit, cerrar} =
        useFormulario(rolService, rolInicial, 'Rol guardado', cargar, guardarRol);

    const handleEditar = (rol) => {
        setPermisosSeleccionados((rol.permisos || []).map(p => p.id));
        handleEditarBase(rol);
    };

    const handleEliminar = async (rol) => {
        if (window.confirm('¿Estás seguro de eliminar este rol?')) {
            try {
                await rolService.eliminar(rol.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al eliminar rol');
            }
        }
    };

    const renderPermisos = () => (
        <div className="form-group">
            <label>Permisos</label>
            <div className="checkbox-list">
                {permisos.map(permiso => (
                    <div className="checkbox-item" key={permiso.id}>
                        <label>
                            <input
                                type="checkbox"
                                checked={permisosSeleccionados.includes(permiso.id)}
                                onChange={() => togglePermiso(permiso.id)}
                            />
                            <span className="permiso-codigo">{permiso.codigoPermiso}</span>
                            <span className="permiso-nombre">{permiso.nombrePermiso}</span>
                        </label>
                    </div>
                ))}
            </div>
        </div>
    );

    return (
        <div>
            <PageHeader
                titulo="Gestión de Roles"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Rol"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Rol"
                    editando={editandoId}
                    campos={camposRol}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                    renderExtra={renderPermisos}
                />
            )}
            <EstadoCarga cargando={loading}>
                {roles.map(rol => (
                    <div key={rol.id} style={{marginBottom: '15px'}}>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                            <div>
                                <h3>{rol.nombreRol}</h3>
                                <p>{rol.descripcion || 'Sin descripción'}</p>
                                <p style={{fontSize: '12px', color: '#666'}}>
                                    Creado: {new Date(rol.fechaCreacion).toLocaleString()}
                                </p>
                            </div>
                            <span className={`badge ${rol.activo ? 'badge-success' : 'badge-danger'}`}>
                                {rol.activo ? 'Activo' : 'Inactivo'}
                            </span>
                        </div>
                        <div style={{marginTop: '10px'}}>
                            <h4>Permisos Asignados:</h4>
                            <div style={{display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '8px'}}>
                                {rol.permisos?.length > 0 ? (
                                    rol.permisos.map(p => (
                                        <span key={p.id} style={{backgroundColor: '#e9ecef', padding: '4px 9px', fontSize: '12px'}}>
                                            {p.codigoPermiso}
                                        </span>
                                    ))
                                ) : (
                                    <span style={{color: '#666', fontSize: '12px'}}>Sin permisos asignados</span>
                                )}
                            </div>
                        </div>
                        {isAdmin && (
                            <div style={{marginTop: '10px'}}>
                                <Acciones
                                    fila={rol}
                                    onEditar={handleEditar}
                                    onEliminar={handleEliminar}
                                    habilitadoEliminar={rol.activo}
                                />
                            </div>
                        )}
                    </div>
                ))}
            </EstadoCarga>
        </div>
    );
};

export default RolesLista;