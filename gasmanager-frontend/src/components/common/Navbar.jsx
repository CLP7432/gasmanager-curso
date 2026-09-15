import React from "react";
import {useLocation} from "react-router-dom";
import {useAuth} from "../../contexts/AuthContext.jsx";

const Navbar = () => {
    const location = useLocation();
    const {user} = useAuth();

    const getPageTitle = () =>{
        const path = location.pathname;

        if(path === '/dashboard') return 'Dashboard';
        if(path === '/usuarios') return 'Gestion de Usuarios';
        return 'GasManager';
    }

    return (
        <div className="navbar">
            <div className="navbar-title">{getPageTitle()}</div>
            <div className="navbar-user">
                <span>👋 Hola, {user?.nombre || user?.correo}</span>
            </div>
        </div>
    );
};
export default Navbar;