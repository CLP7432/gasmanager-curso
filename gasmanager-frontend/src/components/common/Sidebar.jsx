import React from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {NavLink, useNavigate} from "react-router-dom";
import {usePermisos} from "../../kernel/hooks/usePermisos.js";

const Sidebar = () => {

    const {user, logout} = useAuth();
    const {puede, puedeAlguno} = usePermisos();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    }

    return (
        <div className="sidebar">
            <div className="sidebar-header">
                <h2>GasManager</h2>
                <p>Sistema de Gestión</p>
            </div>
            <div className="sidebar-menu">
                <NavLink to="/dashboard" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                    <span className="menu-icon">🏠</span>
                    <span>Dashboard</span>
                </NavLink>

                <div className="menu-group-title">MÓDULOS</div>

                {puede('ADMINISTRACION_VER') && (
                    <NavLink to="/admin" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">⚙️</span>
                        <span>Administración</span>
                    </NavLink>
                )}
                {puede('CLIENTES_VER') && (
                    <NavLink to="/clientes" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">👥</span>
                        <span>Clientes</span>
                    </NavLink>
                )}
                {puede('INVENTARIOS_VER') && (
                    <NavLink to="/inventarios" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">🛢️</span>
                        <span>Inventarios</span>
                    </NavLink>
                )}
                {puede('INVENTARIOS_VER') && (
                    <NavLink to="/ventas" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">💰</span>
                        <span>Ventas</span>
                    </NavLink>
                )}
                {puede('NOMINA_VER') && (
                    <NavLink to="/nomina" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">🧾</span>
                        <span>Nómina</span>
                    </NavLink>
                )}
                {puede('INVENTARIOS_VER') && (
                    <NavLink to="/compras" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">📦</span>
                        <span>Compras</span>
                    </NavLink>
                )}
                {puedeAlguno(['INVENTARIOS_VER', 'CLIENTES_VER']) && (
                    <NavLink to="/facturacion" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">🧾</span>
                        <span>Facturación</span>
                    </NavLink>
                )}
                {puedeAlguno(['INVENTARIOS_VER', 'CLIENTES_VER']) && (
                    <NavLink to="/lealtad" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">⭐</span>
                        <span>Lealtad</span>
                    </NavLink>
                )}
                {puedeAlguno(['INVENTARIOS_VER', 'CLIENTES_VER', 'ADMINISTRACION_VER']) && (
                    <NavLink to="/reportes" className={({isActive}) => `menu-item ${isActive ? 'active' : ''}`}>
                        <span className="menu-icon">📊</span>
                        <span>Reportes</span>
                    </NavLink>
                )}
            </div>
            <div className="sidebar-footer">
                <div className="user-info">
                    <span>👤</span>
                    <div>
                        <div>{user?.correo}</div>
                        <div className="user-role">{user?.rol}</div>
                    </div>
                </div>
                <button className="btn-logout" onClick={handleLogout}>
                    Cerrar Sesión
                </button>
            </div>
        </div>
    );
}
export default Sidebar;