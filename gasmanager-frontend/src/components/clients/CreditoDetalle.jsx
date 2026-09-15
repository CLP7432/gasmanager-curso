import React, {useState, useEffect} from "react";
import {useParams, useNavigate, useLocation} from "react-router-dom";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {creditosService, notasCreditoService} from "../../api/clients/auth.js";
import CardDatos from "../../kernel/components/CardDatos.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import {campoTexto, campoNumero, campoFecha, campoSelect, campoTextarea} from "../../kernel/helpers/campos.jsx";

const abonoInicial = {monto: '', fechaAbono: new Date().toISOString().split('T')[0], metodoPago: 'EFECTIVO', referenciaPago: '', notas: ''};
const camposAbono = [
    campoNumero('monto', 'Monto', {required: true, step: '0.01'}),
    campoFecha('fechaAbono', 'Fecha', {required: true}),
    campoSelect('metodoPago', 'Método de Pago', [
        {value: 'EFECTIVO', label: 'Efectivo'},
        {value: 'TRANSFERENCIA', label: 'Transferencia'},
        {value: 'TARJETA_CREDITO', label: 'Tarjeta Crédito'},
        {value: 'TARJETA_DEBITO', label: 'Tarjeta Débito'},
        {value: 'CHEQUE', label: 'Cheque'}
    ], {required: true}),
    campoTexto('referenciaPago', 'Referencia'),
    campoTextarea('notas', 'Notas')
];
const columnasAbonos = [
    {key: 'folioAbono', label: 'Folio'},
    {key: 'fechaAbono', label: 'Fecha'},
    {key: 'monto', label: 'Monto'},
    {key: 'metodoPago', label: 'Método'},
    {key: 'referenciaPago', label: 'Referencia'},
    {key: 'notas', label: 'Notas'}
];
const columnasNotas = [
    {key: 'numero', label: 'Folio'},
    {key: 'saldo', label: 'Total'},
    {key: 'estado', label: 'Estado'},
    {key: 'fechaCarga', label: 'Fecha de Carga'}
];

const CreditoDetalle = () => {
    const {id} = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const {isAdmin} = useAuth();
    const [credito, setCredito] = useState(null);
    const [abonos, setAbonos] = useState([]);
    const [notas, setNotas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [mostrarForm, setMostrarForm] = useState(false);
    const [objeto, setObjeto] = useState(abonoInicial);
    const [enviando, setEnviando] = useState(false);

    useEffect(() => { cargarDatos(); }, [id]);

    const cargarDatos = async () => {
        setLoading(true);
        try {
            const [c, a, n] = await Promise.all([
                creditosService.obtenerPorId(id),
                creditosService.listarAbonos(id),
                notasCreditoService.listarPorCredito(id)
            ]);
            setCredito(c);
            setAbonos(Array.isArray(a) ? a : []);
            setNotas(Array.isArray(n) ? n : []);
        } catch (error) {
            console.error('Error:', error);
        }
        setLoading(false);
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        setObjeto(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!objeto.monto || parseFloat(objeto.monto) <= 0) return alert('Monto inválido');
        setEnviando(true);
        try {
            await creditosService.registrarAbono(id, {...objeto, monto: parseFloat(objeto.monto)});
            alert('Abono registrado');
            setMostrarForm(false);
            cargarDatos();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al registrar abono');
        }
        setEnviando(false);
    };

    if (loading) return <EstadoCarga cargando={true}/>;
    if (!credito) return <p>Crédito no encontrado.</p>;

    return (
        <div>
            <button className="btn btn-secondary" style={{marginBottom: '20px'}} onClick={() => navigate(location.state?.from || '/clientes/creditos')}>
                    ← {location.state?.from === '/clientes/notas-credito' ? 'Volver a Notas de Crédito' : 'Volver a Créditos'}</button>

            <CardDatos
                titulo={`Crédito ${credito.folioCredito}`}
                objeto={credito}
                campos={[
                    {label: 'Cliente', campo: 'clienteNombre'},
                    {label: 'Monto Total', campo: 'montoTotal'},
                    {label: 'Saldo Pendiente', campo: 'saldoPendiente'},
                    {label: 'Estado', campo: 'estado'},
                    {label: 'Vencimiento', campo: 'fechaVencimiento'},
                    {label: 'Método Pago', campo: 'metodoPago'}
                ]}
            />

            {isAdmin && (credito.estado === 'ACTIVO' || credito.estado === 'VENCIDO') && (
                <div style={{margin: '20px 0'}}>
                    <button className="btn btn-primary" onClick={() => {setObjeto(abonoInicial); setMostrarForm(true);}}>+ Registrar Abono</button>
                </div>
            )}

            {mostrarForm && (
                <FormularioBase
                    titulo="Abono"
                    campos={camposAbono}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={() => setMostrarForm(false)}
                />
            )}

            <h3 style={{marginTop: '25px', marginBottom: '15px'}}>Historial de Abonos</h3>
            {abonos.length === 0 ? (
                <p style={{color: '#888'}}>No hay abonos registrados.</p>
            ) : (
                <TablaDinamica columnas={columnasAbonos} datos={abonos}/>
            )}

            <h3 style={{marginTop: '25px', marginBottom: '15px'}}>Notas de Crédito
                <button className="btn btn-outline-secondary btn-sm" style={{marginLeft: '10px'}}
                        onClick={() => navigate('/clientes/notas-credito', {
                            state: {clienteId: credito.clienteId, clienteNombre: credito.clienteNombre}
                        })}>
                    Ver todas las notas del cliente
                </button>
            </h3>
            {notas.length === 0 ? (
                <p style={{color: '#888'}}>No hay notas de crédito.</p>
            ) : (
                <TablaDinamica columnas={columnasNotas} datos={notas}/>
            )}
        </div>
    );
};

export default CreditoDetalle;