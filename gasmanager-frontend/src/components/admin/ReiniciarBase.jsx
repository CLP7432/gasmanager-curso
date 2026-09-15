import React, {useState} from "react";
import {adminService} from "../../api/admin/auth.js";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";

const ReiniciarBase = () => {
    const [cargando, setCargando] = useState(false);
    const [ids, setIds] = useState([]);
    const [mensaje, setMensaje] = useState('');

    const consultarIds = async () => {
        setCargando(true);
        setMensaje('');
        try {
            setIds(await adminService.proximosIds());
        } catch (error) {
            alert(error.response?.data?.mensaje || error.message || 'Error al consultar los IDs');
        } finally {
            setCargando(false);
        }
    };

    const reiniciar = async () => {
        const confirmar = window.confirm(
            '¿Reiniciar la base de datos a cero?\n\nSe borrarán: ventas, cortes, turnos, dispensarios, entregas, surtidor de aceites, clientes, créditos, compras, proveedores, combustibles, empleados, nóminas, facturas y lealtad.\nSe ponen los stocks en 0.\n\nSE CONSERVAN usuarios, roles, permisos y catálogos.\n\nEsta acción NO se puede deshacer.'
        );
        if (!confirmar) return;
        setCargando(true);
        setMensaje('');
        try {
            const resultado = await adminService.resetBase();
            setMensaje(resultado.mensaje || 'Base de datos reiniciada correctamente.');
            setIds(resultado.proximosIds || []);
        } catch (error) {
            alert(error.response?.data?.mensaje || error.message || 'Error al reiniciar la base de datos');
        } finally {
            setCargando(false);
        }
    };

    return (
        <>
            <PageHeader titulo="Reiniciar base de datos" subtitulo="Deja el sistema como una gasolinera nueva: sin registros operativos y con saldos en cero." mostrarAccion={false}/>
            <EstadoCarga cargando={cargando} mensaje="Procesando...">
                <div className="card" style={{maxWidth: '1000px'}}>
                    <div className="card-body">
                        <div className="alert alert-warning">
                            <strong>⚠️ Esta acción borra los datos operativos</strong> de ventas, cortes, turnos, dispensarios, entregas,
                            surtidor de aceites, clientes, créditos, notas de crédito, compras, proveedores, empleados, nóminas,
                            facturas, registros fiscales y lealtad (programas y puntos),
                            y deja el stock físico en 0. <br/>
                            <strong>Se conservan:</strong> usuarios, roles, permisos y catálogos (aceites, puestos),
                            precios y el admin. Los próximos IDs vuelven a empezar en 1.
                        </div>
                        <div className="d-flex gap-2 mb-3">
                            <button className="btn btn-danger" onClick={reiniciar} disabled={cargando}>
                                ♻️ Reiniciar base de datos a cero
                            </button>
                            <button className="btn btn-outline-secondary" onClick={consultarIds} disabled={cargando}>
                                Ver próximos IDs
                            </button>
                        </div>

                        {mensaje && <div className="alert alert-success">{mensaje}</div>}

                        {ids.length > 0 && (
                            <table className="table table-sm table-striped table-bordered mb-0"
                                   style={{maxWidth: '720px'}}>
                                <thead>
                                <tr>
                                    <th>Base de datos</th>
                                    <th>Tabla</th>
                                    <th className="text-end">Próximo ID</th>
                                </tr>
                                </thead>
                                <tbody>
                                {ids.map((item, index) => (
                                    <tr key={index}>
                                        <td>{item.base}</td>
                                        <td>{item.tabla}</td>
                                        <td className="text-end">{item.proximo_id}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>
            </EstadoCarga>
        </>
    );
};

export default ReiniciarBase;