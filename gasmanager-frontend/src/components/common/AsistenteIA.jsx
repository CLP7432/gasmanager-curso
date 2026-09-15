import React, { useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext.jsx';

const mensajeInicial = {
    id: 1,
    texto: "¡Hola! Soy GasManager Assistant. Pregúntame sobre esta pantalla o tus datos.",
    esUsuario: false,
    timestamp: new Date()
};

const sugerenciasPorModulo = {
    VENTAS: ['¿Cómo abro un turno?', 'Resumen del turno abierto', '¿Qué es un corte?'],
    CLIENTES: ['Créditos con saldo', '¿Cómo liquido notas?', '¿Qué es una nota de crédito?'],
    INVENTARIOS: ['Stock bajo de aceites', 'Nivel de tanques', '¿Cómo compro combustible?'],
    NOMINA: ['Incidencias registradas', '¿Cómo proceso nómina?', 'Tipos de incidencia'],
    FACTURACION: ['¿Cómo facturo un ticket?', 'Notas por facturar', '¿Qué es el uso CFDI?'],
    ADMIN: ['¿Cómo creo un usuario?', '¿Qué son los permisos?', 'Ver auditoría'],
    COMPRAS: ['¿Cómo registro una compra?', 'Ver reporte de compras', '¿Qué es descargar pipa?'],
    GENERAL: ['¿Qué puedo hacer aquí?', 'Resumen de la estación', '¿Cómo empiezo un turno?']
};

const detectarContexto = (path) => {
    if (path.includes('/facturacion')) return 'FACTURACION';
    if (path.includes('/nomina')) return 'NOMINA';
    if (path.includes('/compras')) return 'COMPRAS';
    if (path.includes('/ventas')) return 'VENTAS';
    if (path.includes('/clientes')) return 'CLIENTES';
    if (path.includes('/inventarios')) return 'INVENTARIOS';
    if (path.includes('/admin') || path.includes('/usuarios') || path.includes('/roles')
        || path.includes('/permisos') || path.includes('/auditoria') || path.includes('/reiniciar')) return 'ADMIN';
    return 'GENERAL';
};

const AsistenteIA = () => {
    const { user } = useAuth();
    const location = useLocation();
    const [isOpen, setIsOpen] = useState(false);
    const [mensajes, setMensajes] = useState([mensajeInicial]);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const contexto = detectarContexto(location.pathname);
    const mensajesEndRef = useRef(null);

    useEffect(() => {
        const guardados = localStorage.getItem('asistenteIA_mensajes');
        if (guardados) {
            try {
                setMensajes(JSON.parse(guardados).map(msg => ({...msg, timestamp: new Date(msg.timestamp)})));
            } catch (e) {
                console.error('Error cargando mensajes:', e);
            }
        }
    }, []);

    useEffect(() => {
        if (mensajes.length > 0) {
            localStorage.setItem('asistenteIA_mensajes', JSON.stringify(mensajes));
        }
    }, [mensajes]);

    useEffect(() => {
        if (mensajesEndRef.current) {
            mensajesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [mensajes, isOpen]);

    const enviarTexto = async (texto) => {
        if (!texto.trim() || loading) return;
        const mensajeUsuario = {id: Date.now(), texto, esUsuario: true, timestamp: new Date()};
        setMensajes(prev => [...prev, mensajeUsuario]);
        setInput('');
        setLoading(true);
        try {
            const token = localStorage.getItem('token');
            const response = await fetch('/api/ia/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(token ? {Authorization: `Bearer ${token}`} : {})
                },
                body: JSON.stringify({
                    mensaje: texto,
                    contexto,
                    usuarioId: String(user?.id ?? user?.idUsuario ?? ''),
                    usuarioNombre: user?.correo || 'Usuario'
                })
            });
            const data = await response.json();
            setMensajes(prev => [...prev, {
                id: Date.now() + 1,
                texto: data.respuesta || 'Lo siento, no pude procesar tu solicitud.',
                esUsuario: false,
                timestamp: new Date(),
                error: !data.exito
            }]);
        } catch (error) {
            console.error('Error al enviar mensaje:', error);
            setMensajes(prev => [...prev, {
                id: Date.now() + 1,
                texto: 'Error de conexión. Verifica que el servicio de IA esté corriendo e intenta de nuevo.',
                esUsuario: false,
                timestamp: new Date(),
                error: true
            }]);
        } finally {
            setLoading(false);
        }
    };

    const enviarMensaje = () => enviarTexto(input);

    const limpiarHistorial = () => {
        if (window.confirm('¿Borrar todo el historial de conversación?')) {
            setMensajes([mensajeInicial]);
            localStorage.removeItem('asistenteIA_mensajes');
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            enviarMensaje();
        }
    };

    const formatearHora = (fecha) => {
        if (!fecha) return '';
        return new Date(fecha).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
    };

    const sugerencias = sugerenciasPorModulo[contexto] || sugerenciasPorModulo.GENERAL;

    return (
        <>
            <button
                onClick={() => setIsOpen(!isOpen)}
                title="Ayuda IA"
                style={{
                    position: 'fixed', bottom: '20px', right: '20px',
                    width: '60px', height: '60px', borderRadius: '50%',
                    backgroundColor: '#198754', color: 'white', border: 'none',
                    cursor: 'pointer', boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    zIndex: 1000, display: 'flex', alignItems: 'center',
                    justifyContent: 'center', fontSize: '24px'
                }}
            >
                {isOpen ? '✕' : '🤖'}
            </button>

            {isOpen && (
                <div style={{
                    position: 'fixed', bottom: '90px', right: '20px',
                    width: '380px', maxWidth: 'calc(100vw - 40px)', height: '550px', maxHeight: 'calc(100vh - 120px)',
                    backgroundColor: 'white', borderRadius: '16px',
                    boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
                    display: 'flex', flexDirection: 'column', overflow: 'hidden', zIndex: 1000
                }}>
                    <div style={{
                        background: 'linear-gradient(135deg, #0f172a 0%, #198754 100%)',
                        color: 'white', padding: '12px 16px',
                        display: 'flex', alignItems: 'center', gap: '12px'
                    }}>
                        <div style={{
                            width: '36px', height: '36px', backgroundColor: 'white',
                            borderRadius: '50%', display: 'flex', alignItems: 'center',
                            justifyContent: 'center', fontSize: '20px'
                        }}>🤖</div>
                        <div style={{flex: 1}}>
                            <h4 style={{margin: 0, fontSize: '14px'}}>GasManager Assistant</h4>
                            <small style={{opacity: 0.8, fontSize: '10px'}}>Módulo: {contexto}</small>
                        </div>
                        <button onClick={limpiarHistorial} title="Limpiar historial" style={{
                            background: 'rgba(255,255,255,0.2)', border: 'none', borderRadius: '8px',
                            color: 'white', padding: '4px 8px', fontSize: '11px', cursor: 'pointer'
                        }}>🗑️</button>
                        <div style={{fontSize: '11px', opacity: 0.8}}>{loading ? '✍️' : '●'}</div>
                    </div>

                    <div style={{
                        flex: 1, overflowY: 'auto', padding: '12px', backgroundColor: '#f8f9fa',
                        display: 'flex', flexDirection: 'column', gap: '10px'
                    }}>
                        {mensajes.map((msg) => (
                            <div key={msg.id} style={{display: 'flex', justifyContent: msg.esUsuario ? 'flex-end' : 'flex-start'}}>
                                <div style={{
                                    maxWidth: '80%', padding: '8px 12px', borderRadius: '16px',
                                    backgroundColor: msg.esUsuario ? '#198754' : 'white',
                                    color: msg.esUsuario ? 'white' : '#333',
                                    boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                                    border: msg.esUsuario ? 'none' : '1px solid #e0e0e0'
                                }}>
                                    <div style={{fontSize: '13px', lineHeight: '1.4', whiteSpace: 'pre-wrap'}}>{msg.texto}</div>
                                    <div style={{fontSize: '9px', marginTop: '4px', opacity: 0.6, textAlign: msg.esUsuario ? 'right' : 'left'}}>
                                        {formatearHora(msg.timestamp)}{msg.error && ' ⚠️'}
                                    </div>
                                </div>
                            </div>
                        ))}
                        {loading && (
                            <div style={{display: 'flex', justifyContent: 'flex-start'}}>
                                <div style={{backgroundColor: 'white', padding: '8px 12px', borderRadius: '16px', border: '1px solid #e0e0e0', display: 'flex', gap: '4px'}}>
                                    <span className="dot-pulse">●</span>
                                    <span className="dot-pulse">●</span>
                                    <span className="dot-pulse">●</span>
                                </div>
                            </div>
                        )}
                        <div ref={mensajesEndRef} />
                    </div>

                    <div style={{
                        padding: '8px 12px', backgroundColor: '#f1f3f5',
                        borderTop: '1px solid #e0e0e0', display: 'flex', gap: '6px', flexWrap: 'wrap'
                    }}>
                        {sugerencias.map((s, idx) => (
                            <button key={idx} disabled={loading} onClick={() => enviarTexto(s)} style={{
                                backgroundColor: 'white', border: '1px solid #dee2e6', borderRadius: '20px',
                                padding: '4px 10px', fontSize: '10px', cursor: 'pointer', color: '#495057'
                            }}>{s}</button>
                        ))}
                    </div>

                    <div style={{padding: '10px', display: 'flex', gap: '8px', backgroundColor: 'white'}}>
                        <input
                            type="text" value={input} onChange={(e) => setInput(e.target.value)}
                            onKeyPress={handleKeyPress} placeholder="Escribe tu pregunta..."
                            style={{flex: 1, padding: '8px 12px', border: '1px solid #ddd', borderRadius: '20px', outline: 'none', fontSize: '13px'}}
                            disabled={loading}
                        />
                        <button onClick={enviarMensaje} disabled={loading || !input.trim()} style={{
                            backgroundColor: '#198754', color: 'white', border: 'none', borderRadius: '50%',
                            width: '34px', height: '34px', cursor: 'pointer',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            opacity: loading || !input.trim() ? 0.5 : 1
                        }}>➤</button>
                    </div>
                </div>
            )}

            <style>{`
                .dot-pulse { animation: pulse 1.4s infinite; animation-fill-mode: both; }
                .dot-pulse:nth-child(1) { animation-delay: -0.32s; }
                .dot-pulse:nth-child(2) { animation-delay: -0.16s; }
                @keyframes pulse { 0%, 80%, 100% { opacity: 0.3; } 40% { opacity: 1; } }
            `}</style>
        </>
    );
};

export default AsistenteIA;
