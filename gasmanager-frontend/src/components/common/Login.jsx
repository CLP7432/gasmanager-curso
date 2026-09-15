import {useEffect, useState} from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useNavigate} from "react-router-dom";

const Login = () =>{

    const [correo, setCorreo] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const { login, error, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if(isAuthenticated){
            navigate('/dashboard');
        }
    },[isAuthenticated, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        const result = await login(correo, password);
        setLoading(false);
        if(result.success){
            navigate('/dashboard');
        }
    };

    return (

        <div className="login-container d-flex justify-content-center align-items-center" style={{minHeight: '100vh'}}>
            <div className="card shadow" style={{width:400}}>
                <div className="card-body p-4">
                    <h2 className="text-center mb-1">GasManager</h2>
                    <h5 className="text-center text-muted mb-4">Iniciar Sesión</h5>

                    {error && (
                        <div className="alert alert-danger" role="alert">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label className="form-label">Correo Electrónico</label>
                            <input
                                type="email"
                                value={correo}
                                onChange={(e) => setCorreo(e.target.value)}
                                required
                                className="form-control"
                                placeholder="admin@gasmanager.com"
                            />
                        </div>
                        <div className="mb-3">
                            <label className="form-label">Contraseña</label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                className="form-control"
                                placeholder="********"
                            />
                        </div>
                        <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                            {loading ? 'Iniciando...' : 'Iniciar Sesión'}
                        </button>
                    </form>
                </div>
            </div>

        </div>

    );
}

export default Login;