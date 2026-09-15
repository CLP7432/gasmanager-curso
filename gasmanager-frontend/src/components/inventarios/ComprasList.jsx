import React, {useState, useRef} from "react";
import {campoTexto, campoFecha, campoSelect} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {comprasService, proveedoresService, aceitesService, combustiblesService} from "../../api/inventarios/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import CompraDetalleEditor from "./CompraDetalleEditor.jsx";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
const money = (v) => dinero(v);

const compraInicial = {
    folioFactura: '',
    proveedorId: '',
    fechaFactura: new Date().toISOString().slice(0, 10)
};

const ComprasList = () => {
    const {isAdmin} = useAuth();
    const {datos: compras, loading, cargar} = useLista(comprasService, 'listar');
    const {datos: proveedores} = useLista(proveedoresService, 'listarActivos');
    const {datos: aceites} = useLista(aceitesService, 'listarActivos');
    const {datos: combustibles} = useLista(combustiblesService, 'listarActivos');
    const [items, setItems] = useState([]);
    const [detalle, setDetalle] = useState(null);
    const agregarRef = useRef(null);

    const camposCompra = [
        campoTexto('folioFactura', 'Folio de Factura', {required: true}),
        campoSelect('proveedorId', 'Proveedor',
            proveedores.map(p => ({value: p.id, label: p.razonSocial})),
            {required: true}),
        campoFecha('fechaFactura', 'Fecha de Factura', {required: true})
    ];

    const {mostrarForm, editandoId, objeto, handleNuevo, handleChange, handleSubmit, cerrar} =
        useFormulario(comprasService, compraInicial, 'Compra registrada', cargar,
            (objeto) => {
                if (items.length === 0) {
                    throw {response: {data: {message: 'Debe registrar al menos un producto'}}};
                }
                // El precio de compra nunca debe superar al de venta
                for (const it of items) {
                    const precioCompra = Number(it.precioUnitario) || 0;
                    if (it.tipoProducto === 'COMBUSTIBLE' && it.combustibleId) {
                        const c = combustibles.find(x => String(x.id) === String(it.combustibleId));
                        if (c && precioCompra > Number(c.precioActual)) {
                            throw {response: {data: {message: `El precio de compra de ${c.nombre} ($${precioCompra.toFixed(2)}) no debe ser mayor al precio de venta ($${Number(c.precioActual).toFixed(2)})`}}};
                        }
                    }
                    if (it.tipoProducto === 'ACEITE' && it.aceiteId) {
                        const a = aceites.find(x => String(x.id) === String(it.aceiteId));
                        if (a && precioCompra > Number(a.precioVenta)) {
                            throw {response: {data: {message: `El precio de compra de ${a.nombre} ($${precioCompra.toFixed(2)}) no debe ser mayor al precio de venta ($${Number(a.precioVenta).toFixed(2)})`}}};
                        }
                    }
                }
                const detalles = items.map(({productoNombre, presentacion, subtotal, ...d}) => ({
                    ...d,
                    aceiteId: d.aceiteId ? Number(d.aceiteId) : null,
                    combustibleId: d.combustibleId ? Number(d.combustibleId) : null,
                    presentacion: presentacion || null,
                    cajas: parseInt(d.cajas, 10) || 0,
                    piezas: parseInt(d.piezas, 10) || 0,
                    unidadesPorCaja: parseInt(d.unidadesPorCaja, 10) || 1,
                    cantidad: Number(d.cantidad) || 0,
                    precioUnitario: Number(d.precioUnitario) || 0
                }));
                return comprasService.registrar({
                    ...objeto,
                    proveedorId: Number(objeto.proveedorId),
                    detalles
                });
            });

    const abrirNuevo = () => { setItems([]); handleNuevo(); };
    const cerrarForm = () => { setItems([]); cerrar(); };

    const columnas = [
        idCol,
        columna('folioFactura', 'Folio'),
        columna('proveedorRazonSocial', 'Proveedor'),
        columna('fechaFactura', 'Fecha'),
        columna('subtotal', 'Subtotal', (c) => money(c.subtotal)),
        columna('iva', 'IVA', (c) => money(c.iva)),
        columna('total', 'Total', (c) => <strong style={{color: '#198754'}}>{money(c.total)}</strong>),
        estadoCol((c) => c.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Compras y Facturas"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Registrar Compra"
                onAccion={abrirNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Compra"
                    editando={editandoId}
                    campos={camposCompra}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrarForm}
                    compacto
                    renderExtra={() => (
                        <div style={{marginTop: '15px'}}>
                            <CompraDetalleEditor items={items} onChange={setItems}
                                                  aceites={aceites} combustibles={combustibles}
                                                  agregarRef={agregarRef}/>
                        </div>
                    )}
                    botonesExtra={(
                        <button type="button" className="btn btn-sm btn-primary" onClick={() => agregarRef.current?.()}>+ Agregar producto</button>
                    )}
                />
            )}
            {detalle && (
                <div className="card mb-3" style={{maxWidth: '760px'}}>
                    <div className="card-header d-flex justify-content-between align-items-center" style={{background: '#0f172a', color: '#fff'}}>
                        <strong>Compra {detalle.folioFactura} — {detalle.proveedorRazonSocial}</strong>
                        <button className="btn btn-sm btn-secondary" onClick={() => setDetalle(null)}>Cerrar</button>
                    </div>
                    <div className="card-body" style={{fontSize: '0.9em'}}>
                        {detalle.detalles.map((d, i) => (
                            <div key={i} className="d-flex gap-2" style={{borderBottom: '1px solid #f1f3f5', padding: '3px 0'}}>
                                <span style={{flex: 2}}>{d.productoNombre} ({d.presentacion})</span>
                                <span style={{flex: 1}}>x{d.cantidad}</span>
                                <span style={{flex: 1}}>${d.precioUnitario}</span>
                                <span style={{flex: 1, textAlign: 'right'}}>${d.subtotal}</span>
                            </div>
                        ))}
                        <div className="d-flex justify-content-end gap-4" style={{marginTop: '8px'}}>
                            <span>Subtotal: ${detalle.subtotal}</span>
                            <span>IVA: ${detalle.iva}</span>
                            <span><strong>Total: ${detalle.total}</strong></span>
                        </div>
                    </div>
                </div>
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={compras}
                    acciones={(compra) => (
                        <Acciones
                            fila={compra}
                            onExtra={() => setDetalle(compra)}
                            etiquetaExtra="Detalle"
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default ComprasList;