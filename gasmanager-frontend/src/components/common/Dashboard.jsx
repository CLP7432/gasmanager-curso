import React from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useNavigate} from "react-router-dom";

const Dashboard = () => {

    const {user} = useAuth();
    const navigate = useNavigate();

    const modules = [
        {
            name: 'Administración',
            icon: '⚙️',
            path: '/admin',
            color: '#667eea',
            description: 'Usuarios, roles, permisos y auditoría'
        },
        {
            name: 'Clientes',
            icon: '👥',
            path: '/clientes',
            color: '#20c997',
            description: 'Registro y gestión de clientes'
        },
        {
            name: 'Inventarios',
            icon: '🛢️',
            path: '/inventarios',
            color: '#fd7e14',
            description: 'Control de productos y stock de la estación'
        },
        {
            name: 'Nómina',
            icon: '🧾',
            path: '/nomina',
            color: '#198754',
            description: 'Empleados, puestos, incidencias y cálculo de nóminas'
        },
        {
            name: 'Compras',
            icon: '📦',
            path: '/compras',
            color: '#6f42c1',
            description: 'Registro de compras, facturas y proveedores'
        },
        {
            name: 'Ventas',
            icon: '💰',
            path: '/ventas',
            color: '#fd7e14',
            description: 'Punto de venta, turnos, cortes y dispensarios'
        },
        {
            name: 'Facturación',
            icon: '🧾',
            path: '/facturacion',
            color: '#0d6efd',
            description: 'Emisión de CFDI, facturas y clientes fiscales'
        },
        {
            name: 'Lealtad',
            icon: '⭐',
            path: '/lealtad',
            color: '#b8860b',
            description: 'Programa de puntos por ticket de compra'
        },
        {
            name: 'Reportes',
            icon: '📊',
            path: '/reportes',
            color: '#6f42c1',
            description: 'Resumen, ventas y compras con gráficas y CSV'
        }
    ];

    return (
        <div>
            <div className="welcome-header" style={{
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                borderRadius: '15px',
                padding: '30px',
                marginBottom: '30px',
                color: 'white'
            }}>
                <h1 style={{marginBottom: '10px', fontSize: '28px'}}>
                    ¡Bienvenido, {user?.nombre || user?.correo}!
                </h1>
                <p style={{opacity: 0.9, marginBottom: 0}}>
                    GasManager - Sistema Integral de Gestión para Estaciones de Servicio
                </p>
            </div>
            <h2 style={{marginBottom: '20px', color: '#333'}}>Módulos del Sistema</h2>

            <div className="modules-grid" style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                gap: '20px',
                marginBottom: '40px'
            }}>
                {modules.map((module, index) => (
                    <div
                        key={index}
                        className="module-card"
                        onClick={() => navigate(module.path)}
                        style={{
                            backgroundColor: 'white',
                            borderRadius: '12px',
                            padding: '20px',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                            cursor: 'pointer',
                            transition: 'transform 0.2s, box-shadow 0.2s',
                            borderTop: `4px solid ${module.color}`,
                            position: 'relative'
                        }}
                    >
                        <div style={{fontSize: '40px', marginBottom: '10px'}}>
                            {module.icon}
                        </div>
                        <h3 style={{marginBottom: '8px', color: '#333'}}>{module.name}</h3>
                        <p style={{color: '#666', fontSize: '12px', marginBottom: '10px'}}>
                            {module.description}
                        </p>
                        <span style={{
                            position: 'absolute',
                            bottom: '15px',
                            right: '15px',
                            fontSize: '11px',
                            color: module.color,
                            fontWeight: 'bold'
                        }}>
                            Acceder →
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Dashboard;