import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloNominaPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Nómina"
                subtitulo="Empleados, puestos, incidencias y cálculo de nóminas"
                icono="🧾"
                gradiente="linear-gradient(135deg, #198754 0%, #0f172a 100%)"
                submodulos={[
                    {
                        name: 'Empleados',
                        icon: '👷',
                        path: '/nomina/empleados',
                        color: '#198754',
                        description: 'Alta, edición y estado de empleados: datos personales, salario, puesto y departamento.'
                    },
                    {
                        name: 'Puestos',
                        icon: '💼',
                        path: '/nomina/puestos',
                        color: '#0d6efd',
                        description: 'Catálogo de puestos: salario base, diario y nivel de riesgo.'
                    },
                    {
                        name: 'Departamentos',
                        icon: '🏢',
                        path: '/nomina/departamentos',
                        color: '#6f42c1',
                        description: 'Catálogo de departamentos de la estación.'
                    },
                    {
                        name: 'Incidencias',
                        icon: '⚠️',
                        path: '/nomina/incidencias',
                        color: '#fd7e14',
                        description: 'Faltas, retardos, horas extra, bonos y permisos que afectan la nómina.'
                    },
                    {
                        name: 'Nóminas',
                        icon: '🧾',
                        path: '/nomina/nominas',
                        color: '#e63946',
                        description: 'Procesa el periodo, calcule sueldos, impuestos y deducciones; paga o cancela.'
                    },
                ]}
            />
        </Layout>
    )
}
export default ModuloNominaPage;