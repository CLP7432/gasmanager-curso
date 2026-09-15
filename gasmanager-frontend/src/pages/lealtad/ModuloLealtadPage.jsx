import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloLealtadPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Lealtad"
                subtitulo="Puntos por ticket cuando el programa está activo"
                icono="⭐"
                gradiente="linear-gradient(135deg, #b8860b 0%, #0f172a 100%)"
                submodulos={[
                    {
                        name: 'Programas',
                        icon: '🎯',
                        path: '/lealtad/programas',
                        color: '#b8860b',
                        description: 'Crea el programa, ponle detalles y actívalo para que los tickets den puntos.'
                    },
                    {
                        name: 'Puntos por ticket',
                        icon: '⭐',
                        path: '/lealtad/cuentas',
                        color: '#198754',
                        description: 'Puntos otorgados en cada ticket de compra.'
                    }
                ]}
            />
        </Layout>
    )
}
export default ModuloLealtadPage;
