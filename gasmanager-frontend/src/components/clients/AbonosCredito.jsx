import React, {useEffect, useState} from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useNavigate} from "react-router-dom";
import {creditosService} from "../../api/clients/auth.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import CardDatos from "../../kernel/components/CardDatos.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import {campoNumero, campoFecha, campoSelect, campoTexto, campoTextarea} from "../../kernel/helpers/campos.jsx";

const abonoInicial = {
    monto: '',
    fechaAbono: '',
    metodoPago: 'EFECTIVO',
    referenciaPago: '',
    notas: ''
};
const camposAbono = [
    campoNumero('monto', 'Monto', {required: true, step: '0.01'}),
    campoFecha('fechaAbono', 'Fecha de Abono', {required: true}),
    campoSelect('metodoPago', 'Método de Pago', [
        {value: 'EFECTIVO', label: 'Efectivo'},
        {value: 'TRANSFERENCIA', label: 'Transferencia'},
        {value: 'TARJETA', label: 'Tarjeta'}
    ], {required: true}),
    campoTexto('referenciaPago', 'Referencia de Pago'),
    campoTextarea('notas', 'Notas')
];

const columnasAbonos = [
    {key: 'id', label: 'ID'},
    {key: 'folioAbono', label: 'Folio'},
    {key: 'fechaAbono', label: 'Fecha'},
    {key: 'monto', label: 'Monto'},
    {key: 'metodoPago', label: 'Método'},
    {key: 'referenciaPago', label: 'Referencia'},
    {key: 'notas', label: 'Notas'}
];

const AbonosCredito = ({creditoId}) => {
    const [credito, setCredito] = useState(null);
    const [abonos, setAbonos] = useState([]);
    const [loading, setLoading] = useState(true);
    const {isAdmin} = useAuth();
    const navigate = useNavigate();

    const cargarAbonos = async () => {
        setLoading(true);
        try {
            const data = await creditosService.obtenerPorId(creditoId);
            setCredito(data);
            setAbonos(data.abonos || []);
        } catch (error) {
            console.error('Error al cargar crédito: ', error);
        }
        setLoading(false);
    };

    useEffect(() => {
        cargarAbonos();
    }, [creditoId]);

    const registrarAbono = (objeto) => {
        return creditosService.registrarAbono(creditoId, objeto);
    };

    const {mostrarForm, objeto, handleNuevo, handleChange, handleSubmit, cerrar} =
        useFormulario(creditosService, abonoInicial, 'Abono registrado', cargarAbonos, registrarAbono);

    if (loading) {
        return <p>Cargando crédito...</p>;
    }
    if (!credito) {
        return <p>No se encontró el crédito.</p>;
    }

    return (
        <div>
            <button className="btn btn-secondary" style={{marginBottom: '20px'}}
                    onClick={() => navigate('/clientes/creditos')}>
                ← Volver a Créditos
            </button>
            <CardDatos
                titulo="Resumen del Crédito"
                objeto={credito}
                campos={[
                    {label: 'Folio', campo: 'folioCredito'},
                    {label: 'Cliente', campo: 'clienteNombre'},
                    {label: 'Monto Total', campo: 'montoTotal'},
                    {label: 'Saldo Pendiente', campo: 'saldoPendiente'},
                    {label: 'Estado', campo: 'estado'}
                ]}
            />
            {isAdmin && (
                <div style={{margin: '20px 0'}}>
                    <button className="btn btn-primary" onClick={handleNuevo}>
                        + Registrar Abono
                    </button>
                </div>
            )}
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Abono"
                    campos={camposAbono}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                />
            )}
            <div style={{marginTop: '20px'}}>
                <TablaDinamica columnas={columnasAbonos} datos={abonos}/>
            </div>
        </div>
    );
};

export default AbonosCredito;