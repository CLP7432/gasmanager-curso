import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloFacturacionPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Facturación"
                subtitulo="Emisión de CFDI, XML/PDF y control de clientes fiscales"
                icono="🧾"
                gradiente="linear-gradient(135deg, #0d6efd 0%, #0f172a 100%)"
                submodulos={[
                    {
                        name: 'Facturas',
                        icon: '📄',
                        path: '/facturacion/facturas',
                        color: '#0d6efd',
                        description: 'Crear facturas desde tickets, notas o conceptos manuales. XML/PDF incluidos.'
                    },
                    {
                        name: 'Clientes Fiscales',
                        icon: '🏷️',
                        path: '/facturacion/clientes-fiscales',
                        color: '#198754',
                        description: 'Registros fiscales de clientes: RFC, régimen y domicilio.'
                    },
                    {
                        name: 'Notas por facturar',
                        icon: '📝',
                        path: '/facturacion/notas-por-facturar',
                        color: '#fd7e14',
                        description: 'Consumos a crédito pendientes de factura: elige cliente y continúa a facturar.'
                    }
                ]}
            />
        </Layout>
    )
}
export default ModuloFacturacionPage;
