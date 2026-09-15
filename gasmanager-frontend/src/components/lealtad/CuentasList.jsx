import React from "react";
import {lealtadService} from "../../api/lealtad/auth.js";
import {columna} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";

const CuentasList = () => {
    const {datos, loading} = useLista(lealtadService, 'cuentas');

    const total = (datos || []).reduce((s, c) => s + (Number(c.puntos) || 0), 0);

    const columnas = [
        columna('folioVenta', 'Ticket'),
        columna('litros', 'Litros', (v) => Number(v.litros || 0).toFixed(2)),
        columna('puntos', 'Puntos', (v) => <strong style={{color: '#b8860b'}}>⭐ {v.puntos}</strong>)
    ];

    return (
        <div>
            <PageHeader titulo="Puntos por ticket" subtitulo={`Total otorgado: ⭐ ${total} puntos`} mostrarAccion={false} />
            <EstadoCarga cargando={loading}>
                <TablaDinamica columnas={columnas} datos={datos} />
            </EstadoCarga>
        </div>
    );
};
export default CuentasList;
