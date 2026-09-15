import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloComprasPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Compras"
                subtitulo="Registro de compras, facturas y proveedores"
                icono="🧾"
                gradiente="linear-gradient(135deg, #6f42c1 0%, #0f172a 100%)"
                submodulos={[
                    {
                        name: 'Compras y Facturas',
                        icon: '🧾',
                        path: '/compras/facturas',
                        color: '#6f42c1',
                        description: 'Registro de compras por factura: actualiza stock y precios, calcula IVA para impuestos.'
                    },
                    {
                        name: 'Proveedores',
                        icon: '🏭',
                        path: '/compras/proveedores',
                        color: '#0d6efd',
                        description: 'Alta y catálogo de proveedores (PEMEX para combustibles, otros para aceites).'
                    },
                    {
                        name: 'Reporte Mensual',
                        icon: '📊',
                        path: '/compras/reporte',
                        color: '#198754',
                        description: 'Compendio mensual de facturas para impuestos: suma subtotal, IVA y total.'
                    },
                ]}
            />
        </Layout>
    )
}
export default ModuloComprasPage;