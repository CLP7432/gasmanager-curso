import React from "react";
import Layout from "../../components/common/Layout.jsx";
import ModuloPage from "../../kernel/components/ModuloPage.jsx";

const ModuloAdminPage = () => {
    return (
        <Layout>
            <ModuloPage
                titulo="Módulo de Administración"
                subtitulo="Gestión de usuarios, roles, permisos y auditoría"
                icono="⚙️"
                gradiente="linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
                submodulos={[
                    {name: 'Usuarios', icon: '👥', path: '/usuarios', color: '#667eea', description: 'Gestión de usuarios del sistema: alta, edición y estado.'},
                    {name: 'Roles', icon: '🛡️', path: '/roles', color: '#20c997', description: 'Roles con permisos asignados para el control de acceso.'},
                    {name: 'Permisos', icon: '🔑', path: '/permisos', color: '#fd7e14', description: 'Permisos y códigos que se asignan a cada rol.'},
                    {name: 'Auditoría', icon: '📋', path: '/auditoria', color: '#6c757d', description: 'Registro de actividades realizadas en el sistema.'},
                    {name: 'Reiniciar Base', icon: '♻️', path: '/reiniciar-base', color: '#e74c3c', description: 'Reinicia la base de datos a cero para pruebas; conserva catálogos y admin.'}
                ]}
            />
        </Layout>
    )
}
export default ModuloAdminPage;