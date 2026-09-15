import React from "react";
import Layout from "../../components/common/Layout.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import TanqueNivel from "../../components/inventarios/TanqueNivel.jsx";
import {tanquesService, combustiblesService} from "../../api/inventarios/auth.js";
import {columna} from "../../kernel/helpers/columnas.jsx";
import {campoTexto, campoNumero, campoSelect} from "../../kernel/helpers/campos.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";

const tanqueInicial = {nombre: '', combustibleId: '', capacidadLitros: '', stockLitros: ''};

const TanquesNivelesPage = () => {
    const {datos: tanques, loading, cargar} = useLista(tanquesService, 'listar');
    const {datos: combustibles} = useLista(combustiblesService, 'listar');
    const form = useFormulario(tanquesService, tanqueInicial, 'Tanque guardado', cargar);
    const activos = tanques.filter(t => t.activo);

    const guardarTanque = async (e) => {
        e.preventDefault();
        const payload = {
            ...form.objeto,
            combustibleId: form.objeto.combustibleId ? Number(form.objeto.combustibleId) : null,
            capacidadLitros: form.objeto.capacidadLitros === '' ? null : Number(form.objeto.capacidadLitros),
            stockLitros: form.objeto.stockLitros === '' || form.objeto.stockLitros == null ? 0 : Number(form.objeto.stockLitros)
        };
        try {
            if (form.editandoId) await tanquesService.actualizar(form.editandoId, payload);
            else await tanquesService.crear(payload);
            alert('Tanque guardado');
            form.cerrar();
            cargar();
        } catch (error) {
            const data = error.response?.data;
            alert(typeof data === 'string' ? data : (data?.message || 'Error al guardar el tanque'));
        }
    };

    const campos = [
        campoTexto('nombre', 'Nombre', {required: true, placeholder: 'Ej. Tanque Magna 1'}),
        campoSelect('combustibleId', 'Combustible', combustibles.map(c => ({value: c.id, label: `${c.nombre} (${c.tipo})`})), {required: true}),
        campoNumero('capacidadLitros', 'Capacidad (L)', {required: true, min: 1}),
        campoNumero('stockLitros', 'Stock inicial (L)', {min: 0})
    ];

    const columnas = [
        columna('nombre', 'Tanque'),
        columna('tipoCombustible', 'Producto'),
        columna('capacidadLitros', 'Capacidad (L)'),
        columna('stockLitros', 'Stock (L)')
    ];

    if (loading) return <Layout><EstadoCarga cargando/></Layout>;

    return (
        <Layout>
            <div className="d-flex justify-content-between align-items-center">
                <PageHeader
                    titulo="Niveles de Tanques"
                    etiquetaAccion="+ Nuevo tanque"
                    onAccion={form.handleNuevo}
                />
                <button className="btn btn-outline-primary" onClick={cargar}>↻ Actualizar</button>
            </div>
            {form.mostrarForm && (
                <FormularioBase
                    titulo="Tanque de almacenamiento"
                    editando={!!form.editandoId}
                    campos={campos}
                    objeto={form.objeto}
                    handleChange={form.handleChange}
                    handleSubmit={guardarTanque}
                    cerrar={form.cerrar}
                />
            )}
            <div className="d-flex flex-wrap gap-3 mb-3">
                {activos.map(t => (
                    <TanqueNivel key={t.id} tanque={t}/>
                ))}
            </div>
            <TablaDinamica
                columnas={columnas}
                datos={tanques}
                acciones={(t) => (
                    <Acciones fila={t} onEditar={form.handleEditar} />
                )}
            />
            <div className="text-muted small mt-2">
                Mostrando {activos.length} de {tanques.length} tanques activos.
            </div>
        </Layout>
    );
};

export default TanquesNivelesPage;