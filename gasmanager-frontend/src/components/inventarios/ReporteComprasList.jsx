import React, {useState} from "react";
import {comprasService} from "../../api/inventarios/auth.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import CardDatos from "../../kernel/components/CardDatos.jsx";
import {columna} from "../../kernel/helpers/columnas.jsx";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const hoy = new Date();
const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

const ReporteComprasList = () => {
    const [anio, setAnio] = useState(hoy.getFullYear());
    const [mes, setMes] = useState(hoy.getMonth() + 1);
    const [compras, setCompras] = useState([]);
    const [loading, setLoading] = useState(false);

    const consultar = async () => {
        setLoading(true);
        try {
            setCompras(await comprasService.reporteMensual(anio, mes));
        } catch {
            alert('Error al consultar el reporte');
        }
        setLoading(false);
    };

    const totalSub = compras.reduce((acc, c) => acc + (c.subtotal || 0), 0);
    const totalIva = compras.reduce((acc, c) => acc + (c.iva || 0), 0);
    const total = totalSub + totalIva;

    const columnas = [
        columna('folioFactura', 'Folio'),
        columna('proveedorRazonSocial', 'Proveedor'),
        columna('fechaRegistro', 'Fecha'),
        columna('subtotal', 'Subtotal', (c) => dinero(c.subtotal)),
        columna('iva', 'IVA', (c) => dinero(c.iva)),
        columna('total', 'Total', (c) => dinero(c.total))
    ];

    return (
        <div>
            <PageHeader titulo={`Reporte Mensual de Compras ${meses[mes - 1]} ${anio}`} mostrarAccion={false}/>
            <div
                style={{display: 'flex', gap: '12px', alignItems: 'flex-end', marginBottom: '15px', maxWidth: '760px'}}>
                <div>
                    <label>Año</label>
                    <input className="form-control" type="number" value={anio}
                           onChange={(e) => setAnio(e.target.value)}/>
                </div>
                <div>
                    <label>Mes</label>
                    <select className="form-control" value={mes} onChange={(e) => setMes(e.target.value)}>
                        {meses.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
                    </select>
                </div>
                <button className="btn btn-primary" onClick={consultar}>Consultar</button>
            </div>
            <EstadoCarga cargando={loading}>
                <TablaDinamica columnas={columnas} datos={compras}/>
            </EstadoCarga>
            {compras.length > 0 && (
                <CardDatos
                    titulo="Totales del mes"
                    objeto={{
                        Facturas: String(compras.length),
                        Subtotal: dinero(totalSub),
                        IVA: dinero(totalIva),
                        Total: dinero(total)
                    }}
                    campos={[
                        {label: 'Facturas', campo: 'Facturas'},
                        {label: 'Subtotal', campo: 'Subtotal'},
                        {label: 'IVA', campo: 'IVA'},
                        {label: 'Total', campo: 'Total'}
                    ]}
                />
            )}
        </div>
    );
};

export default ReporteComprasList;