
import React, {createContext, useState, useContext, useEffect} from "react";
import {authService} from "../api/admin/auth.js";

const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if(!context){
        throw new Error('useAuth debe usarse dentro de AuthProvider');
    }
    return context;
};

export const AuthProvider = ({children}) =>{
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const currentUser = authService.getCurrentUser();
        if(currentUser){
            authService.validarToken()
                .then(esValido => {
                    if(esValido){
                        setUser(currentUser);
                    }else{
                        authService.logout();
                    }
                })
                .catch(() => {
                    authService.logout();
                })
                .finally(() => setLoading(false));
        }else{
            setLoading(false);
        }
    }, []);

    const login = async (correo, password) =>{
        setError(null);
        try {
            const data = await authService.login(correo, password);
            setUser(data);
            return { success: true, data};
        }catch (err){
            const errorMsg = err.response?.data?.error ||
                err.response?.data?.mensaje ||
                'Error al iniciar sesión';
            setError(errorMsg);
            return {success: false, error: errorMsg};
        }
    };

    const logout = () => {
        authService.logout();
        setUser(null);
    };

    const isAdmin = user?.rol === 'ADMIN';

    const value = {
        user,
        loading,
        error,
        login,
        logout,
        isAuthenticated: !!user,
        isAdmin,
    };
    return(
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

