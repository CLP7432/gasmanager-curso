import React from "react";
import {programaService} from "../../api/lealtad/auth.js";
import {columna} from "../../kernel/helpers/columnas.jsx";
import {campoTexto, campoNumero, campoFecha, campoTextarea} from "../../kernel/helpers/campos.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";

const objetoInicial = {
    nombre: '',
    descripcion: '',
    puntosPorLitro: 1,
    fechaInicio: '',
    fechaFin: ''
};

const camposPrograma = [
    campoTexto('nombre', 'Nombre del programa', {required: true, placeholder: 'Ej. Puntos GasManager'}),
    campoTextarea('descripcion', 'Detalles (cómo gana y usa puntos el cliente)'),
    campoNumero('puntosPorLitro', 'Puntos por litro', {required: true, min: 1}),
    campoFecha('fechaInicio', 'Vigencia desde'),
    campoFecha('fechaFin', 'Vigencia hasta')
];

const ProgramaList = () => {
    const {datos, loading, cargar} = useLista(programaService, 'listar');
    const form = useFormulario(programaService, objetoInicial, 'Programa guardado', cargar);

    const activar = async (fila) => {
        if (!window.confirm(`¿Activar "${fila.nombre}"? Se apaga cualquier otro programa.`)) return;
        try {
            await programaService.activar(fila.id);
            cargar();
        } catch {
            alert('No se pudo activar');
        }
    };

    const desactivar = async (fila) => {
        if (!window.confirm(`¿Desactivar "${fila.nombre}"? Los tickets dejarán de dar puntos.`)) return;
        try {
            await programaService.desactivar(fila.id);
            cargar();
        } catch {
            alert('No se pudo desactivar');
        }
    };

    const columnas = [
        columna('id', 'ID'),
        columna('nombre', 'Programa'),
        columna('puntosPorLitro', 'Pts/L'),
        columna('fechaInicio', 'Desde', (v) => v.fechaInicio || '-'),
        columna('fechaFin', 'Hasta', (v) => v.fechaFin || '-'),
        columna('activo', 'Estado', (v) => v.activo
            ? <span className="badge bg-success">ACTIVO</span>
            : <span className="badge bg-secondary">Inactivo</span>)
    ];

    return (
        <div>
            <PageHeader
                titulo="Programas de lealtad"
                subtitulo="Solo el programa activo da puntos en los tickets"
                etiquetaAccion="+ Nuevo programa"
                onAccion={form.handleNuevo}
            />
            {form.mostrarForm && (
                <FormularioBase
                    titulo="Programa"
                    editando={!!form.editandoId}
                    campos={camposPrograma}
                    objeto={form.objeto}
                    handleChange={form.handleChange}
                    handleSubmit={form.handleSubmit}
                    cerrar={form.cerrar}
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={datos}
                    acciones={(fila) => (
                        <Acciones
                            fila={fila}
                            onEditar={form.handleEditar}
                            onToggle={fila.activo ? desactivar : activar}
                            etiquetaToggle={fila.activo ? 'Desactivar' : 'Activar'}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
};
export default ProgramaList;
