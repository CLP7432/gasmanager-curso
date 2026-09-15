import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloReportesPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Reportes"
                subtitulo="Agregados de ventas, compras y resumen (sin microservicio: directo de las APIs)"
                icono="📊"
                gradiente="linear-gradient(135deg, #0f172a 0%, #6f42c1 100%)"
                submodulos={[
                    {
                        name: 'Resumen general',
                        icon: '📌',
                        path: '/reportes/resumen',
                        color: '#0d6efd',
                        description: 'Ventas, compras, margen, créditos y facturación del periodo.'
                    },
                    {
                        name: 'Reporte de Ventas',
                        icon: '💰',
                        path: '/reportes/ventas',
                        color: '#198754',
                        description: 'Tickets por fecha, estado y pago, con gráfica y CSV.'
                    },
                    {
                        name: 'Reporte de Compras',
                        icon: '📦',
                        path: '/reportes/compras',
                        color: '#b8860b',
                        description: 'Facturas de compra por fecha, con gráfica y CSV.'
                    }
                ]}
            />
        </Layout>
    )
}
export default ModuloReportesPage;
