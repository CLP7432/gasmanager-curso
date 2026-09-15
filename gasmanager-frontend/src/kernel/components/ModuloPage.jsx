import React from "react";
import {useNavigate} from "react-router-dom";

const ModuloPage = ({titulo, subtitulo, icono, gradiente, submodulos}) => {
    const navigate = useNavigate();

    return (
        <div>
            <div className="welcome-header" style={{
                background: gradiente || 'linear-gradient(135deg, #0f172a 0%, #334155 100%)',
                borderRadius: '15px',
                padding: '30px',
                marginBottom: '30px',
                color: 'white'
            }}>
                <h1 style={{marginBottom: '10px', fontSize: '28px'}}>
                    {icono} {titulo}
                </h1>
                <p style={{opacity: 0.9, marginBottom: 0}}>
                    {subtitulo}
                </p>
            </div>

            <h2 style={{marginBottom: '20px', color: '#333'}}>Submódulos disponibles</h2>

            <div className="modules-grid" style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
                gap: '20px',
                marginBottom: '40px'
            }}>
                {submodulos.map((modulo, index) => (
                    <div
                        key={index}
                        className="module-card"
                        onClick={() => navigate(modulo.path)}
                        style={{
                            backgroundColor: 'white',
                            borderRadius: '12px',
                            padding: '20px',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                            cursor: 'pointer',
                            transition: 'transform 0.2s, box-shadow 0.2s',
                            borderTop: `4px solid ${modulo.color}`,
                            position: 'relative'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-5px)';
                            e.currentTarget.style.boxShadow = '0 8px 20px rgba(0,0,0,0.15)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                        }}
                    >
                        <div style={{fontSize: '40px', marginBottom: '10px'}}>
                            {modulo.icon}
                        </div>
                        <h3 style={{marginBottom: '8px', color: '#333'}}>{modulo.name}</h3>
                        <p style={{color: '#666', fontSize: '12px', marginBottom: '10px'}}>
                            {modulo.description}
                        </p>
                        <span style={{
                            position: 'absolute',
                            bottom: '15px',
                            right: '15px',
                            fontSize: '11px',
                            color: modulo.color,
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

export default ModuloPage;