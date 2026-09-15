import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloVentasPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Ventas"
                subtitulo="Punto de venta y control de ventas de la estación"
                icono="💰"
                gradiente="linear-gradient(135deg, #ffc107 0%, #0f172a 100%)"
                submodulos={[
                    {
                        name: 'Punto de Venta',
                        icon: '⛽',
                        path: '/ventas/punto-venta',
                        color: '#28a745',
                        description: 'Simulador de carga de combustible: litros o pesos, con avance en tiempo real.'
                    },
                    {
                        name: 'Turnos',
                        icon: '🕧',
                        path: '/ventas/turnos',
                        color: '#6c757d',
                        description: 'Abrir y cerrar turnos de trabajo (uno a la vez).'
                    },
                    {
                        name: 'Cortes',
                        icon: '📊',
                        path: '/ventas/cortes',
                        color: '#dc3545',
                        description: 'Generar, validar y cerrar cortes por turno e isla; la diferencia dispara la incidencia FALTANTE.'
                    },
                    {
                        name: 'Reporte Mensual',
                        icon: '📊',
                        path: '/ventas/reporte',
                        color: '#6f42c1',
                        description: 'Ventas del mes con totales: litros, subtotal, IVA y total.'
                    },
                    {
                        name: 'Historial de Ventas',
                        icon: '📋',
                        path: '/ventas/historial',
                        color: '#17a2b8',
                        description: 'Consulta de ventas realizadas, filtro por estado y cancelación.'
                    },
                ]}
            />
        </Layout>
    )
}
export default ModuloVentasPage;