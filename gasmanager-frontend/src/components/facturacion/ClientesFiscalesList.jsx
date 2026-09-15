import React, {useMemo, useState} from "react";
import {clientesFiscalesService} from "../../api/facturacion/auth.js";
import {clientesService} from "../../api/clients/auth.js";
import {columna} from "../../kernel/helpers/columnas.jsx";
import {campoTexto, campoEmail, campoSelect} from "../../kernel/helpers/campos.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";

// Nodo Receptor CFDI 4.0 (SAT): RFC + Nombre exacto + C.P. domicilio fiscal
// + Régimen + Uso. Teléfono, correo y dirección se guardan en el módulo
// de clientes; aquí solo viaja lo que timbra el CFDI.
const objetoInicial = {
    clienteId: '',
    tipoPersona: 'MORAL',
    nombre: '',
    rfc: '',
    razonSocial: '',
    regimenFiscal: '601',
    codigoPostal: '',
    usoCfdi: 'G03',
    correo: '',
    telefono: '',
    calle: '',
    numeroExterior: '',
    colonia: '',
    ciudad: '',
    estado: ''
};

const REGIMENES = [
    {value: '601', label: '601 - General de Ley'},
    {value: '603', label: '603 - Fines no lucrativos'},
    {value: '605', label: '605 - Sueldos y Salarios'},
    {value: '606', label: '606 - Arrendamiento'},
    {value: '612', label: '612 - Personas Físicas con Actividad'},
    {value: '616', label: '616 - Sin obligaciones fiscales'},
    {value: '626', label: '626 - RESICO'}
];

const USOS = [
    {value: 'G01', label: 'G01 - Adquisición de mercancías'},
    {value: 'G03', label: 'G03 - Gastos en general'},
    {value: 'S01', label: 'S01 - Sin efectos fiscales'}
];

const ClientesFiscalesList = () => {
    const {datos, loading, cargar} = useLista(clientesFiscalesService, 'listar');
    const {datos: clientes, cargar: recargarClientes} = useLista(clientesService, 'listar');
    const [modo, setModo] = useState('existente');

    const guardar = async (objeto, editandoId) => {
        if (editandoId) {
            await clientesFiscalesService.actualizar(editandoId, {
                rfc: objeto.rfc,
                razonSocial: objeto.razonSocial,
                regimenFiscal: objeto.regimenFiscal,
                codigoPostal: objeto.codigoPostal,
                usoCfdi: objeto.usoCfdi,
                correo: objeto.correo
            });
            return;
        }
        if (modo === 'nuevo') {
            // 1) Alta del cliente (el backend genera el código consecutivo CLI-XXX)
            const razon = objeto.razonSocial || objeto.nombre;
            const nuevo = await clientesService.crear({
                tipoPersona: objeto.tipoPersona,
                nombre: objeto.nombre,
                razonSocial: razon,
                rfc: objeto.rfc,
                email: objeto.correo,
                telefono: objeto.telefono,
                calle: objeto.calle,
                numeroExterior: objeto.numeroExterior,
                colonia: objeto.colonia,
                ciudad: objeto.ciudad,
                estado: objeto.estado,
                codigoPostal: objeto.codigoPostal,
                activo: true
            });
            // 2) Registro fiscal ligado al ID recién creado (sin duplicar captura)
            try {
                await clientesFiscalesService.crear({
                    clienteId: nuevo.id,
                    rfc: objeto.rfc,
                    razonSocial: razon,
                    regimenFiscal: objeto.regimenFiscal,
                    codigoPostal: objeto.codigoPostal,
                    usoCfdi: objeto.usoCfdi,
                    correo: objeto.correo
                });
            } catch (e) {
                throw new Error(`Cliente creado con ID ${nuevo.id}, pero el registro fiscal falló: ${e.response?.data?.message || e.message}`);
            }
            recargarClientes();
            return;
        }
        await clientesFiscalesService.crear({
            clienteId: Number(objeto.clienteId),
            rfc: objeto.rfc,
            razonSocial: objeto.razonSocial,
            regimenFiscal: objeto.regimenFiscal,
            codigoPostal: objeto.codigoPostal,
            usoCfdi: objeto.usoCfdi,
            correo: objeto.correo
        });
    };

    const form = useFormulario(clientesFiscalesService, objetoInicial, 'Registro fiscal guardado', cargar, guardar);

    const nuevoRegistro = () => {
        setModo('existente');
        form.handleNuevo();
    };

    // El ID del registro fiscal es consecutivo automático (IDENTITY en BD).
    // Modo existente: solo clientes sin registro fiscal (no se duplica).
    const clientesDisponibles = useMemo(() => {
        const registrados = new Set((datos || []).map(d => String(d.clienteId)));
        return (clientes || [])
            .filter(c => c.activo !== false)
            .filter(c => !registrados.has(String(c.id)) || String(form.objeto.clienteId) === String(c.id));
    }, [clientes, datos, form.objeto.clienteId]);

    // Al elegir el cliente se autocompletan RFC, razón social, C.P. y correo
    // desde el módulo de clientes. Quedan editables por si hay que corregir.
    const handleChange = (e) => {
        const {name, value} = e.target;
        if (name === 'clienteId') {
            const sel = (clientes || []).find(c => String(c.id) === String(value));
            form.setObjeto(prev => ({
                ...prev,
                clienteId: value,
                rfc: sel?.rfc || prev.rfc,
                razonSocial: sel?.razonSocial || sel?.nombre || sel?.nombreComercial || prev.razonSocial,
                codigoPostal: sel?.codigoPostal || prev.codigoPostal,
                correo: sel?.email || prev.correo,
                telefono: sel?.telefono || prev.telefono
            }));
            return;
        }
        form.handleChange(e);
    };

    const camposExistente = [
        campoSelect('clienteId', 'Cliente registrado', clientesDisponibles.map(c => ({
            value: c.id,
            label: `#${c.id} — ${c.codigoCliente || 's/c'} — ${c.razonSocial || c.nombre || c.nombreComercial || 'sin nombre'}`
        })), {required: true}),
        campoTexto('rfc', 'RFC', {required: true, placeholder: 'Ej. XAXX010101000'}),
        campoTexto('razonSocial', 'Nombre o Razón Social (exacto SAT)'),
        campoSelect('regimenFiscal', 'Régimen Fiscal', REGIMENES, {required: true}),
        campoTexto('codigoPostal', 'C.P. Domicilio Fiscal', {required: true}),
        campoSelect('usoCfdi', 'Uso CFDI', USOS),
        campoEmail('correo', 'Correo')
    ];

    const camposNuevo = [
        campoSelect('tipoPersona', 'Tipo de Persona', [
            {value: 'MORAL', label: 'Persona Moral'},
            {value: 'FISICA', label: 'Persona Física'}
        ], {required: true}),
        campoTexto('nombre', 'Nombre', {required: true}),
        campoTexto('razonSocial', 'Razón Social (si difiere del nombre)'),
        campoTexto('rfc', 'RFC', {required: true, placeholder: 'Ej. XAXX010101000'}),
        campoTexto('telefono', 'Teléfono'),
        campoEmail('correo', 'Correo'),
        campoTexto('calle', 'Calle'),
        campoTexto('numeroExterior', 'No. Exterior'),
        campoTexto('colonia', 'Colonia'),
        campoTexto('ciudad', 'Ciudad'),
        campoTexto('estado', 'Estado'),
        campoTexto('codigoPostal', 'C.P. Domicilio Fiscal', {required: true}),
        campoSelect('regimenFiscal', 'Régimen Fiscal', REGIMENES, {required: true}),
        campoSelect('usoCfdi', 'Uso CFDI', USOS)
    ];

    const columnas = [
        columna('id', 'ID'),
        columna('clienteId', 'ClienteID'),
        columna('rfc', 'RFC'),
        columna('razonSocial', 'Razón Social'),
        columna('regimenFiscal', 'Régimen'),
        columna('codigoPostal', 'C.P.'),
        columna('usoCfdi', 'Uso')
    ];

    const eliminar = async (fila) => {
        if (!window.confirm(`¿Eliminar el registro fiscal ${fila.rfc}?`)) return;
        try {
            await clientesFiscalesService.eliminar(fila.id);
            cargar();
        } catch {
            alert('No se pudo eliminar');
        }
    };

    return (
        <div>
            <PageHeader
                titulo="Clientes Fiscales"
                subtitulo="RFC, razón social exacta, C.P. fiscal, régimen y uso (CFDI 4.0)"
                etiquetaAccion="+ Nuevo registro"
                onAccion={nuevoRegistro}
            />
            {form.mostrarForm && (
                <>
                    {!form.editandoId && (
                        <div className="btn-group mb-2" role="group">
                            <button
                                type="button"
                                className={`btn btn-sm ${modo === 'existente' ? 'btn-primary' : 'btn-outline-primary'}`}
                                onClick={() => setModo('existente')}
                            >
                                Cliente registrado
                            </button>
                            <button
                                type="button"
                                className={`btn btn-sm ${modo === 'nuevo' ? 'btn-primary' : 'btn-outline-primary'}`}
                                onClick={() => setModo('nuevo')}
                            >
                                Cliente nuevo
                            </button>
                        </div>
                    )}
                    <FormularioBase
                        titulo={form.editandoId ? 'Cliente Fiscal' : (modo === 'nuevo' ? 'Cliente Nuevo + Fiscales' : 'Cliente Fiscal')}
                        editando={!!form.editandoId}
                        campos={form.editandoId ? camposExistente.filter(c => c.name !== 'clienteId') : (modo === 'nuevo' ? camposNuevo : camposExistente)}
                        objeto={form.objeto}
                        handleChange={handleChange}
                        handleSubmit={form.handleSubmit}
                        cerrar={form.cerrar}
                    />
                </>
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={datos}
                    acciones={(fila) => (
                        <Acciones
                            fila={fila}
                            onEditar={form.handleEditar}
                            onEliminar={eliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
};
export default ClientesFiscalesList;
