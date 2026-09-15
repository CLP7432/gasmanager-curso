import React from "react";
import {AuthProvider, useAuth} from "./contexts/AuthContext.jsx";
import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom';
import ErrorBoundary from "./kernel/components/ErrorBoundary.jsx";
import LoginPage from "./pages/common/LoginPage.jsx";
import DashboardPage from "./pages/common/DashboardPage.jsx";
import UsuariosPage from "./pages/admin/UsuariosPage.jsx";
import RolesPage from "./pages/admin/RolesPage.jsx";
import PermisosPage from "./pages/admin/PermisosPage.jsx";
import AuditoriaPage from "./pages/admin/AuditoriaPage.jsx";
import ReiniciarBasePage from "./pages/admin/ReiniciarBasePage.jsx";
import ClientesListaPage from "./pages/clients/ClientesListaPage.jsx";
import ModuloAdminPage from "./pages/admin/ModuloAdminPage.jsx";
import ModuloClientesPage from "./pages/clients/ModuloClientesPage.jsx";
import CreditosListaPage from "./pages/clients/CreditosListaPage.jsx";
import AbonosCreditoPage from "./pages/clients/AbonosCreditoPage.jsx";
import LiquidarNotasPage from "./pages/clients/LiquidarNotasPage.jsx";
import ModuloInventariosPage from "./pages/inventarios/ModuloInventariosPage.jsx";
import AceitesListaPage from "./pages/inventarios/AceitesListaPage.jsx";
import CombustiblesListaPage from "./pages/inventarios/CombustiblesListaPage.jsx";
import ProveedoresPage from "./pages/inventarios/ProveedoresPage.jsx";
import ComprasPage from "./pages/inventarios/ComprasPage.jsx";
import CreditoDetalle from "./components/clients/CreditoDetalle.jsx";
import Layout from "./components/common/Layout.jsx";
import NotasCreditoPage from "./pages/clients/NotasCreditoPage.jsx";
import NotaDetallePage from "./pages/clients/NotaDetallePage.jsx";
import PagosListaPage from "./pages/clients/PagosListaPage.jsx";
import CreditoDisponiblePage from "./pages/clients/CreditoDisponiblePage.jsx";
import ReporteComprasPage from "./pages/inventarios/ReporteComprasPage.jsx";
import ModuloVentasPage from "./pages/ventas/ModuloVentasPage.jsx";
import PuntoVentaPage from "./pages/ventas/PuntoVentaPage.jsx";
import VentasPage from "./pages/ventas/VentasPage.jsx";
import TanquesNivelesPage from "./pages/inventarios/TanquesNivelesPage.jsx";
import TanquesCargasPage from "./pages/inventarios/TanquesCargasPage.jsx";
import ReporteVentasPage from "./pages/ventas/ReporteVentasPage.jsx";
import ModuloNominaPage from "./pages/nomina/ModuloNominaPage.jsx";
import EmpleadosPage from "./pages/nomina/EmpleadosPage.jsx";
import PuestosPage from "./pages/nomina/PuestosPage.jsx";
import DepartamentosPage from "./pages/nomina/DepartamentosPage.jsx";
import IncidenciasPage from "./pages/nomina/IncidenciasPage.jsx";
import NominasPage from "./pages/nomina/NominasPage.jsx";
import DispensariosPage from "./pages/ventas/DispensariosPage.jsx";
import TurnosPage from "./pages/ventas/TurnosPage.jsx";
import CortesPage from "./pages/ventas/CortesPage.jsx";
import SurtidorAceitesPage from "./pages/ventas/SurtidorAceitesPage.jsx";
import CambiosPrecioAceitesPage from "./pages/ventas/CambiosPrecioAceitesPage.jsx";
import ModuloComprasPage from "./pages/compras/ModuloComprasPage.jsx";
import CambiosPrecioCombustiblesPage from "./pages/inventarios/CambiosPrecioCombustiblesPage.jsx";
import ModuloFacturacionPage from "./pages/facturacion/ModuloFacturacionPage.jsx";
import FacturasPage from "./pages/facturacion/FacturasPage.jsx";
import ClientesFiscalesPage from "./pages/facturacion/ClientesFiscalesPage.jsx";
import NotasPorFacturarPage from "./pages/facturacion/NotasPorFacturarPage.jsx";
import ModuloLealtadPage from "./pages/lealtad/ModuloLealtadPage.jsx";
import ProgramaPage from "./pages/lealtad/ProgramaPage.jsx";
import CuentasPage from "./pages/lealtad/CuentasPage.jsx";
import ModuloReportesPage from "./pages/reportes/ModuloReportesPage.jsx";
import ResumenPage from "./pages/reportes/ResumenPage.jsx";
import VentasReportePage from "./pages/reportes/VentasReportePage.jsx";
import ComprasReportePage from "./pages/reportes/ComprasReportePage.jsx";

const PrivateRoute = ({children}) => {
    const {isAuthenticated} = useAuth();
    return isAuthenticated ? children : <Navigate to='/login'/>;
};

const AppRoutes = () => {
    const {isAuthenticated} = useAuth();
    return (
        <Routes>
            <Route path='/login' element={<LoginPage/>}/>
            <Route path='/dashboard' element={
                <PrivateRoute><DashboardPage/></PrivateRoute>
            }/>
            <Route path='/' element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'}/>}/>
            <Route path='*' element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'}/>}/>

            {/*    USUARIOS   */}
            <Route path='/usuarios' element={
                <PrivateRoute><UsuariosPage/></PrivateRoute>
            }/>
            {/*  ROLES  */}
            <Route path='/roles' element={
                <PrivateRoute><RolesPage/> </PrivateRoute>
            }/>

            {/*    PERMISOS */}
            <Route path='/permisos' element={
                <PrivateRoute><PermisosPage/></PrivateRoute>
            }/>

            {/*    AUDITORIA */}
            <Route path='/auditoria' element={
                <PrivateRoute><AuditoriaPage/></PrivateRoute>
            }/>

            {/*    REINICIAR BASE */}
            <Route path='/reiniciar-base' element={
                <PrivateRoute><ReiniciarBasePage/></PrivateRoute>
            }/>

            {/*    ADMINISTRACION (módulo) */}
            <Route path='/admin' element={
                <PrivateRoute><ModuloAdminPage/></PrivateRoute>
            }/>

            {/*    CLIENTES (módulo) */}
            <Route path='/clientes' element={
                <PrivateRoute><ModuloClientesPage/></PrivateRoute>
            }/>
            <Route path='/clientes/listar' element={
                <PrivateRoute><ClientesListaPage/></PrivateRoute>
            }/>
            <Route path='/clientes/creditos' element={
                <PrivateRoute><CreditosListaPage/></PrivateRoute>
            }/>
            <Route path='/clientes/creditos/:id/abonos' element={
                <PrivateRoute><AbonosCreditoPage/></PrivateRoute>
            }/>
            <Route path='/clientes/creditos/:id/liquidar' element={
                <PrivateRoute><LiquidarNotasPage/></PrivateRoute>
            }/>
            <Route path='/clientes/creditos/:id' element={
                <PrivateRoute><Layout><CreditoDetalle/></Layout></PrivateRoute>
            }/>
            <Route path='/clientes/notas-credito' element={
                <PrivateRoute><NotasCreditoPage/></PrivateRoute>
            }/>
            <Route path='/clientes/notas-credito/:id' element={
                <PrivateRoute><NotaDetallePage/></PrivateRoute>
            }/>
            <Route path='/clientes/pagos' element={
                <PrivateRoute><PagosListaPage/></PrivateRoute>
            }/>
            <Route path='/clientes/credito-disponible' element={
                <PrivateRoute><CreditoDisponiblePage/></PrivateRoute>
            }/>

            {/*    INVENTARIOS (módulo) */}
            <Route path='/inventarios' element={
                <PrivateRoute><ModuloInventariosPage/></PrivateRoute>
            }/>
            <Route path='/inventarios/aceites' element={
                <PrivateRoute><AceitesListaPage/></PrivateRoute>
            }/>
            <Route path='/inventarios/combustibles' element={
                <PrivateRoute><CombustiblesListaPage/></PrivateRoute>
            }/>
            <Route path='/inventarios/proveedores' element={
                <PrivateRoute><ProveedoresPage/></PrivateRoute>
            }/>
            <Route path='/inventarios/compras' element={
                <PrivateRoute><ComprasPage/></PrivateRoute>
            }/>
            <Route path='/inventarios/compras/reporte' element={
                <ReporteComprasPage/>
            }/>

            <Route path='/inventarios/tanques' element={
                <PrivateRoute><TanquesNivelesPage/></PrivateRoute>
            }/>
            <Route path='/inventarios/tanques/cargas' element={
                <PrivateRoute><TanquesCargasPage/></PrivateRoute>
            }/>

            {/* Dispensarios (catálogo desde Inventarios) */}
            <Route path='/inventarios/dispensarios' element={
                <PrivateRoute><DispensariosPage/></PrivateRoute>
            }/>
            {/* Cambios de precios de aceites */}
            <Route path='/inventarios/cambios-precios-aceites' element={
                <PrivateRoute><CambiosPrecioAceitesPage/></PrivateRoute>
            }/>
            {/* Cambios de precios de combustibles */}
            <Route path='/inventarios/cambios-precios-combustibles' element={
                <PrivateRoute><CambiosPrecioCombustiblesPage/></PrivateRoute>
            }/>
            {/* Surtidor de aceites */}
            <Route path='/inventarios/surtidores-aceite' element={
                <PrivateRoute><SurtidorAceitesPage/></PrivateRoute>
            }/>

            {/*    COMPRAS (módulo) */}
            <Route path='/compras' element={
                <PrivateRoute><ModuloComprasPage/></PrivateRoute>
            }/>
            <Route path='/compras/facturas' element={
                <PrivateRoute><ComprasPage/></PrivateRoute>
            }/>
            <Route path='/compras/proveedores' element={
                <PrivateRoute><ProveedoresPage/></PrivateRoute>
            }/>
            <Route path='/compras/reporte' element={
                <PrivateRoute><ReporteComprasPage/></PrivateRoute>
            }/>

            {/*    VENTAS (módulo) */}
            <Route path='/ventas' element={
                <PrivateRoute><ModuloVentasPage/></PrivateRoute>
            }/>
            <Route path='/ventas/punto-venta' element={
                <PrivateRoute><PuntoVentaPage/></PrivateRoute>
            }/>
            <Route path='/ventas/historial' element={
                <PrivateRoute><VentasPage/></PrivateRoute>
            }/>
            <Route path='/ventas/reporte' element={
                <PrivateRoute><ReporteVentasPage/></PrivateRoute>
            }/>

            {/* Dispensarios */}
            <Route path='/ventas/dispensarios' element={
                <PrivateRoute><DispensariosPage/></PrivateRoute>
            }/>
            {/* Turnos */}
            <Route path='/ventas/turnos' element={
                <PrivateRoute><TurnosPage/></PrivateRoute>
            }/>
            {/* Cortes */}
            <Route path='/ventas/cortes' element={
                <PrivateRoute><CortesPage/></PrivateRoute>
            }/>
            {/* Surtidores de aceites */}
            <Route path='/ventas/surtidores-aceite' element={
                <PrivateRoute><SurtidorAceitesPage/></PrivateRoute>
            }/>
            {/* Cambios de precios de aceites */}
            <Route path='/ventas/cambios-precios-aceites' element={
                <PrivateRoute><CambiosPrecioAceitesPage/></PrivateRoute>
            }/>

            {/*    NOMINA (módulo) */}
            <Route path='/nomina' element={
                <PrivateRoute><ModuloNominaPage/></PrivateRoute>
            }/>
            {/*    FACTURACION */}
            <Route path='/facturacion' element={
                <PrivateRoute><ModuloFacturacionPage/></PrivateRoute>
            }/>
            <Route path='/facturacion/facturas' element={
                <PrivateRoute><FacturasPage/></PrivateRoute>
            }/>
            <Route path='/facturacion/clientes-fiscales' element={
                <PrivateRoute><ClientesFiscalesPage/></PrivateRoute>
            }/>
            <Route path='/facturacion/notas-por-facturar' element={
                <PrivateRoute><NotasPorFacturarPage/></PrivateRoute>
            }/>
            {/*    LEALTAD */}
            <Route path='/lealtad' element={
                <PrivateRoute><ModuloLealtadPage/></PrivateRoute>
            }/>
            <Route path='/lealtad/programas' element={
                <PrivateRoute><ProgramaPage/></PrivateRoute>
            }/>
            <Route path='/lealtad/cuentas' element={
                <PrivateRoute><CuentasPage/></PrivateRoute>
            }/>
            {/*    REPORTES (sin microservicio) */}
            <Route path='/reportes' element={
                <PrivateRoute><ModuloReportesPage/></PrivateRoute>
            }/>
            <Route path='/reportes/resumen' element={
                <PrivateRoute><ResumenPage/></PrivateRoute>
            }/>
            <Route path='/reportes/ventas' element={
                <PrivateRoute><VentasReportePage/></PrivateRoute>
            }/>
            <Route path='/reportes/compras' element={
                <PrivateRoute><ComprasReportePage/></PrivateRoute>
            }/>
            <Route path='/nomina/empleados' element={
                <PrivateRoute><EmpleadosPage/></PrivateRoute>
            }/>
            <Route path='/nomina/puestos' element={
                <PrivateRoute><PuestosPage/></PrivateRoute>
            }/>
            <Route path='/nomina/departamentos' element={
                <PrivateRoute><DepartamentosPage/></PrivateRoute>
            }/>
            <Route path='/nomina/incidencias' element={
                <PrivateRoute><IncidenciasPage/></PrivateRoute>
            }/>
            <Route path='/nomina/nominas' element={
                <PrivateRoute><NominasPage/></PrivateRoute>
            }/>

        </Routes>
    );
};

function App() {
    return (
        <AuthProvider>
            <Router>
                <ErrorBoundary>
                    <AppRoutes/>
                </ErrorBoundary>
            </Router>
        </AuthProvider>
    )
}

export default App;
