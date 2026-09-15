import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloClientesPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Clientes"
                subtitulo="Gestión integral de clientes"
                icono="👥"
                gradiente="linear-gradient(135deg, #20c997 0%, #0f172a 100%)"
                submodulos={[
                    {
                        name: 'Clientes',
                        icon: '👥',
                        path: '/clientes/listar',
                        color: '#20c997',
                        description: 'Registro y gestión de clientes: alta, edición, activación y búsqueda.'
                    },
                    {
                        name: 'Créditos',
                        icon: '💳',
                        path: '/clientes/creditos',
                        color: '#0d6efd',
                        description: 'Gestión de líneas de crédito: alta, abonos, estados, cancelaciones y vencimientos.'
                    },
                    {
                        name: 'Notas de Crédito',
                        icon: '📋',
                        path: '/clientes/notas-credito',
                        color: '#198754',
                        description: 'Registro único de la carga: fecha, cliente y productos (combustible, aceites, aditivos) con totales.'
                    },
                    {
                        name: 'Pagos',
                        icon: '💵',
                        path: '/clientes/pagos',
                        color: '#6f42c1',
                        description: 'Historial completo de pagos y abonos por cliente.'
                    },
                    {
                        name: 'Crédito Disponible',
                        icon: '⛽',
                        path: '/clientes/credito-disponible',
                        color: '#0dcaf0',
                        description: 'Reporte de turno: clientes que aún cuentan con crédito para cargar.'
                    }
                ]}
            />
        </Layout>
    )
}
export default ModuloClientesPage;