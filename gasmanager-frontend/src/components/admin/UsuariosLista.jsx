import React, {useState} from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {rolService, usuarioService} from "../../api/admin/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import {idCol, estadoCol, columna} from "../../kernel/helpers/columnas.jsx";
import {campoTexto, campoEmail, campoPassword, campoSelect} from "../../kernel/helpers/campos.jsx";

const usuarioInicial = {
    nombre: '',
    correo: '',
    password: '',
    rolId: '2'
};
const camposUsuario = (roles) => [
    campoTexto('nombre', 'Nombre', {required: true}),
    campoEmail('correo', 'Correo', {required: true}),
    campoPassword('password', 'Contraseña'),
    campoSelect('rolId', 'Rol', roles.map(r => ({value: String(r.id), label: r.nombreRol})), {required: true})
];

const UsuariosLista = () => {
    const [filtro, setFiltro] = useState('todos');
    const [usuarioInactivo, setUsuarioInactivo] = useState(false);
    const {isAdmin} = useAuth();

    const listarUsuarios = () => {
        const metodos = {todos: 'listar', activos: 'listarActivos', bloqueados: 'listarBloqueados'};
        return metodos[filtro] || 'listar';
    };
    const {datos: usuarios, loading, cargar} = useLista(usuarioService, listarUsuarios(), [filtro]);
    const {datos: roles} = useLista(rolService, 'listarActivos');

    const guardarUsuario = (objeto, editandoId) => {
        const usuarioData = {
            nombre: objeto.nombre,
            correo: objeto.correo,
            password: objeto.password,
            rol: {id: parseInt(objeto.rolId)}
        };
        if (editandoId) {
            return usuarioService.actualizar(editandoId, usuarioData).then(() => {
                if (usuarioInactivo) {
                    return usuarioService.activar(editandoId);
                }
            });
        }
        return usuarioService.crear(usuarioData);
    };

    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar: handleEditarBase, handleChange, handleSubmit, cerrar} =
        useFormulario(usuarioService, usuarioInicial, 'Usuario guardado', cargar, guardarUsuario);

    const handleEditar = (usuario) => {
        setUsuarioInactivo(!usuario.activo);
        handleEditarBase({
            ...usuario,
            rolId: usuario.rol?.id ? String(usuario.rol.id) : '2',
            password: ''
        });
    };

    const handleDesactivar = async (usuario) => {
        if (window.confirm('¿Estás seguro de desactivar este usuario?')) {
            try {
                await usuarioService.desactivar(usuario.id);
                cargar();
            } catch (error) {
                alert('Error al desactivar usuario');
            }
        }
    };

    const columnas = [
        idCol,
        columna('nombre', 'Nombre'),
        columna('correo', 'Correo'),
        columna('rol', 'Rol', (u) => u.rol?.nombreRol || 'Sin rol'),
        columna('intentosFallidos', 'Intentos'),
        {
            key: 'estado',
            label: 'Estado',
            render: (u) => {
                if (u.bloqueado) return <span className="badge badge-danger">Bloqueado</span>;
                if (!u.activo) return <span className="badge badge-warning">Inactivo</span>;
                return <span className="badge badge-success">Activo</span>;
            }
        }
    ];

    const renderAvisoInactivo = () => (
        <div className="error-message" style={{marginBottom: '15px'}}>
            Este usuario está inactivo. Al guardar se reactivará automáticamente.
        </div>
    );

    return (
        <div>
            <PageHeader
                titulo="Gestión de Usuarios"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Usuario"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Usuario"
                    editando={editandoId}
                    campos={camposUsuario(roles)}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                    renderExtra={editandoId && usuarioInactivo ? renderAvisoInactivo : undefined}
                />
            )}
            <div className="card">
                <div style={{marginBottom: '15px'}}>
                    <label>Filtrar:</label>
                    <select value={filtro} onChange={(e) => setFiltro(e.target.value)} style={{marginBottom: '10px', padding: '5px'}}>
                        <option value="todos">Todos</option>
                        <option value="activos">Activos</option>
                        <option value="bloqueados">Bloqueados</option>
                    </select>
                </div>
                <EstadoCarga cargando={loading}>
                    <TablaDinamica
                        columnas={columnas}
                        datos={usuarios}
                        acciones={(usuario) => (
                            <Acciones
                                fila={usuario}
                                onEditar={handleEditar}
                                onEliminar={handleDesactivar}
                                etiquetaEliminar="Desactivar"
                                habilitadoEliminar={usuario.activo}
                            />
                        )}
                    />
                </EstadoCarga>
            </div>
        </div>
    );
};

export default UsuariosLista;