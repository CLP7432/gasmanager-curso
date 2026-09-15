import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloInventariosPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Inventarios"
                subtitulo="Control de productos y stock de la estación"
                icono="🛢️"
                gradiente="linear-gradient(135deg, #fd7e14 0%, #0f172a 100%)"
                submodulos={[
                    {
                        name: 'Aceites',
                        icon: '🛢️',
                        path: '/inventarios/aceites',
                        color: '#fd7e14',
                        description: 'Catálogo y control de stock de aceites: precios, presentaciones y alertas de stock bajo.'
                    },
                    {
                        name: 'Combustibles',
                        icon: '⛽',
                        path: '/inventarios/combustibles',
                        color: '#198754',
                        description: 'Gestión de combustibles: precios, historial y cambio periódico.'
                    },
                    {
                        name: 'Dispensarios',
                        icon: '⛽',
                        path: '/inventarios/dispensarios',
                        color: '#0f766e',
                        description: 'Catálogo de islas: 4 mangueras (A1/A2/B1/B2) y asignar despachador.'
                    },
                    {
                        name: 'Cambios de Precios de Aceites',
                        icon: '💰',
                        path: '/inventarios/cambios-precios-aceites',
                        color: '#6f42c1',
                        description: 'Actualiza el precio de venta del catálogo de aceites (solo encargado); los cortes toman el precio vigente.'
                    },
                    {
                        name: 'Cambios de Precios de Combustibles',
                        icon: '💵',
                        path: '/inventarios/cambios-precios-combustibles',
                        color: '#d63384',
                        description: 'Actualiza el precio por litro de los combustibles con motivo y registro de historial.'
                    },
                    {
                        name: 'Surtidor de Aceites',
                        icon: '🛢️',
                        path: '/inventarios/surtidores-aceite',
                        color: '#b45309',
                        description: 'Control de aceites por surtidor y despachador: inventario inicial, entregas desde bodega y alertas de bajo stock.'
                    },
                    {
                        name: 'Niveles de Tanques',
                        icon: '⛽',
                        path: '/inventarios/tanques',
                        color: '#e63946',
                        description: 'Simulador visual de niveles de combustible por tanque: porcentaje, litros y alerta de nivel bajo.'
                    },
                    {
                        name: 'Carga de Tanques',
                        icon: '🚛',
                        path: '/inventarios/tanques/cargas',
                        color: '#0dcaf0',
                        description: 'Simulador de llegada de la pipa: registra litros cargados al tanque y valida la capacidad disponible.'
                    },
                ]}
            />
        </Layout>
    )
}
export default ModuloInventariosPage;