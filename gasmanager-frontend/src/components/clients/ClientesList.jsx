import React from "react";
import {useNavigate} from "react-router-dom";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {clientesService} from "../../api/clients/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import {campoTexto, campoSelect, campoEmail} from "../../kernel/helpers/campos.jsx";

const clienteInicial = {
    tipoPersona: 'FISICA',
    nombre: '',
    razonSocial: '',
    nombreComercial: '',
    rfc: '',
    curp: '',
    email: '',
    telefono: '',
    celular: '',
    calle: '',
    numeroExterior: '',
    numeroInterior: '',
    colonia: '',
    ciudad: '',
    estado: '',
    codigoPostal: ''
};
const camposCliente = [
    campoSelect('tipoPersona', 'Tipo de Persona', [
        {value: 'FISICA', label: 'Persona Física'},
        {value: 'MORAL', label: 'Persona Moral'}
    ], {required: true}),
    campoTexto('nombre', 'Nombre'),
    campoTexto('razonSocial', 'Razón Social'),
    campoTexto('nombreComercial', 'Nombre Comercial'),
    campoTexto('rfc', 'RFC'),
    campoTexto('curp', 'CURP'),
    campoEmail('email', 'Email'),
    campoTexto('telefono', 'Teléfono'),
    campoTexto('celular', 'Celular'),
    campoTexto('calle', 'Calle'),
    campoTexto('numeroExterior', 'Número Exterior'),
    campoTexto('numeroInterior', 'Número Interior'),
    campoTexto('colonia', 'Colonia'),
    campoTexto('ciudad', 'Ciudad'),
    campoTexto('estado', 'Estado'),
    campoTexto('codigoPostal', 'Código Postal')
];

const ClientesList = () => {
    const {isAdmin} = useAuth();
    const navigate = useNavigate();
    const {datos: clientes, loading, cargar} = useLista(clientesService, 'listar');

    const {mostrarForm, editandoId, objeto, setObjeto, handleNuevo, handleEditar, handleChange, cerrar} =
        useFormulario(clientesService, clienteInicial, 'Cliente guardado', cargar);

    // El backend devuelve el motivo como texto plano ("El RFC ya está registrado"):
    // extraerlo para avisar qué corregir en vez del genérico.
    const guardarCliente = async (e) => {
        e.preventDefault();
        try {
            if (editandoId) await clientesService.actualizar(editandoId, objeto);
            else await clientesService.crear(objeto);
            alert('Cliente guardado');
            cerrar();
            cargar();
        } catch (error) {
            const data = error.response?.data;
            const motivo = typeof data === 'string' ? data : data?.message;
            if (error.response?.status === 409) {
                alert(`No se puede guardar: ${motivo || 'registro duplicado (ese RFC ya existe)'}`);
            } else {
                alert(motivo || 'Error al guardar');
            }
        }
    };

    const handleToggleActivo = async (cliente) => {
        if (window.confirm('¿Estás seguro de cambiar el estado de este cliente?')) {
            try {
                await clientesService.toggleActivo(cliente.id);
                cargar();
            } catch (error) {
                alert('Error al cambiar el estado del cliente');
            }
        }
    };

    const handleEliminar = async (cliente) => {
        if (window.confirm('¿Estás seguro de eliminar este cliente?')) {
            try {
                await clientesService.eliminar(cliente.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al eliminar cliente');
            }
        }
    };

    const columnas = [
        idCol,
        columna('codigoCliente', 'Código'),
        columna('tipoPersona', 'Tipo'),
        columna('nombre', 'Nombre'),
        columna('razonSocial', 'Razón Social'),
        columna('rfc', 'RFC'),
        columna('email', 'Email'),
        estadoCol((c) => c.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Clientes"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Cliente"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Cliente"
                    editando={editandoId}
                    campos={camposCliente}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={guardarCliente}
                    cerrar={cerrar}
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={clientes}
                    acciones={(cliente) => (
                        <Acciones
                            fila={cliente}
                            onExtra={() => navigate('/clientes/notas-credito', {
                                state: {clienteId: cliente.id, clienteNombre: cliente.nombre || cliente.razonSocial || cliente.nombreComercial}
                            })}
                            etiquetaExtra="Ver notas"
                            onEditar={handleEditar}
                            onToggle={handleToggleActivo}
                            etiquetaToggle={cliente.activo ? 'Desactivar' : 'Activar'}
                            onEliminar={handleEliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
};

export default ClientesList;