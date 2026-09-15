import React from "react";
import {useNavigate} from "react-router-dom";
import {creditosService} from "../../api/clients/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import {idCol, columna} from "../../kernel/helpers/columnas.jsx";

const dinero = (valor) => `$${Number(valor).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const CreditoDisponibleList = () => {
    const navigate = useNavigate();
    const {datos: creditos, loading} = useLista(creditosService, 'listarActivosConSaldo');

    const totalDisponible = (creditos || []).reduce((acc, c) => acc + Number(c.saldoPendiente || 0), 0);

    const columnas = [
        idCol,
        columna('clienteNombre', 'Cliente'),
        columna('folioCredito', 'Folio'),
        columna('saldoPendiente', 'Saldo Disponible', (c) => <strong>{dinero(c.saldoPendiente)}</strong>),
        columna('fechaVencimiento', 'Vence'),
        columna('clienteId', 'ID Cliente')
    ];

    return (
        <div>
            <button className="btn btn-secondary btn-sm mb-2" onClick={() => navigate('/clientes')}>
                ← Volver a Clientes
            </button>
            <PageHeader
                titulo="Crédito Disponible"
                subtitulo="Reporte para inicio de turno: clientes que aún cuentan con crédito para cargar"
                etiquetaAccion="+ Nuevo crédito"
                onAccion={() => navigate('/clientes/creditos')}
            />
            <div className="card mb-3" style={{maxWidth: '1000px'}}>
                <div className="card-body">
                    <div className="row">
                        <div className="col-md-6">
                            <small className="text-muted">Clientes con crédito disponible</small>
                            <div className="fs-4 fw-bold">{(creditos || []).length}</div>
                        </div>
                        <div className="col-md-6">
                            <small className="text-muted">Saldo total disponible</small>
                            <div className="fs-4 fw-bold text-success">{dinero(totalDisponible)}</div>
                        </div>
                    </div>
                </div>
            </div>
            <EstadoCarga cargando={loading}>
                <TablaDinamica columnas={columnas} datos={creditos || []}/>
            </EstadoCarga>
        </div>
    );
};

export default CreditoDisponibleList;