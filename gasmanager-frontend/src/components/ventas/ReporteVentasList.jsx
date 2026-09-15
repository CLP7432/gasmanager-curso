import React, {useState} from "react";
import {ventasService} from "../../api/ventas/auth.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import CardDatos from "../../kernel/components/CardDatos.jsx";
import {columna} from "../../kernel/helpers/columnas.jsx";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
const litrosTxt = (v) => `${Number(v).toLocaleString('es-MX', {maximumFractionDigits: 2})} L`;

const hoy = new Date();
const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

const ReporteVentasList = () => {
    const [anio, setAnio] = useState(hoy.getFullYear());
    const [mes, setMes] = useState(hoy.getMonth() + 1);
    const [ventas, setVentas] = useState([]);
    const [loading, setLoading] = useState(false);

    const consultar = async () => {
        setLoading(true);
        try {
            setVentas(await ventasService.reporteMensual(anio, mes));
        } catch {
            alert('Error al consultar el reporte');
        }
        setLoading(false);
    };

    const vigentes = ventas.filter(v => v.estado !== 'CANCELADA');
    const litros = vigentes.reduce((acc, v) =>
        acc + (v.detalles || []).reduce((a, d) =>
            a + (d.tipoProducto === 'COMBUSTIBLE' ? Number(d.cantidad) || 0 : 0), 0), 0);
    const totalSub = vigentes.reduce((acc, v) => acc + (v.subtotal || 0), 0);
    const totalIva = vigentes.reduce((acc, v) => acc + (v.iva || 0), 0);
    const total = totalSub + totalIva;

    const columnas = [
        columna('folio', 'Folio'),
        columna('fechaHora', 'Fecha'),
        columna('metodoPago', 'Método'),
        columna('estado', 'Estado'),
        columna('subtotal', 'Subtotal', (v) => dinero(v.subtotal)),
        columna('iva', 'IVA', (v) => dinero(v.iva)),
        columna('total', 'Total', (v) => dinero(v.total))
    ];

    return (
        <div>
            <PageHeader titulo={`Reporte Mensual de Ventas ${meses[mes - 1]} ${anio}`} mostrarAccion={false}/>
            <div style={{display: 'flex', gap: '12px', alignItems: 'flex-end', marginBottom: '15px', maxWidth: '760px'}}>
                <div>
                    <label>Año</label>
                    <input className="form-control" type="number" value={anio} onChange={(e) => setAnio(e.target.value)}/>
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
                <TablaDinamica columnas={columnas} datos={ventas}/>
            </EstadoCarga>
            {ventas.length > 0 && (
                <CardDatos
                    titulo="Totales del mes (sin canceladas)"
                    objeto={{
                        Ventas: String(vigentes.length),
                        Litros: litrosTxt(litros),
                        Subtotal: dinero(totalSub),
                        IVA: dinero(totalIva),
                        Total: dinero(total)
                    }}
                    campos={[
                        {label: 'Ventas', campo: 'Ventas'},
                        {label: 'Litros', campo: 'Litros'},
                        {label: 'Subtotal', campo: 'Subtotal'},
                        {label: 'IVA', campo: 'IVA'},
                        {label: 'Total', campo: 'Total'}
                    ]}
                />
            )}
        </div>
    );
};

export default ReporteVentasList;