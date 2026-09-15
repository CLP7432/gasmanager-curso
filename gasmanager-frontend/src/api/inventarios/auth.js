import {crearApi} from "../http.js";

const api = crearApi();

//Servicio de Aceites
export const aceitesService = {
    listar: async () => {
        const response = await api.get('/aceites');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/aceites/activos');
        return response.data;
    },
    listarStockBajo: async () => {
        const response = await api.get('/aceites/stock-bajo');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/aceites/${id}`);
        return response.data;
    },
    crear: async (aceite) => {
        const response = await api.post('/aceites', aceite);
        return response.data;
    },
    actualizar: async (id, aceite) => {
        const response = await api.put(`/aceites/${id}`, aceite);
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/aceites/${id}/toggle`);
        return response.data;
    },
    eliminar: async (id) => {
        const response = await api.delete(`/aceites/${id}`);
        return response.data;
    },
    aumentarStock: async (id, cantidad) => {
        const response = await api.post(`/aceites/${id}/aumentar-stock`, null,{params:{cantidad}});
        return response.data;
    },
    disminuirStock: async (id, cantidad) => {
        const response = await api.post(`/aceites/${id}/disminuir-stock`,null,{params:{cantidad}});
        return response.data;
    },
    actualizarPrecio: async (id, precioVenta) => {
        const response = await api.put(`/aceites/${id}/precio`, {precioVenta});
        return response.data;
    }
};

//Servicio de Combustibles
export const combustiblesService = {
    listar: async () => {
        const response = await api.get('/combustibles');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/combustibles/activos');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/combustibles/${id}`);
        return response.data;
    },
    crear: async (combustible) => {
        const response = await api.post('/combustibles', combustible);
        return response.data;
    },
    actualizar: async (id, combustible) => {
        const response = await api.put(`/combustibles/${id}`, combustible);
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/combustibles/${id}/toggle`);
        return response.data;
    },
    cambiarPrecio: async (id, datos) => {
        const response = await api.post(`/combustibles/${id}/cambiar-precio`, datos);
        return response.data;
    },
    listarHistorial: async (id) => {
        const response = await api.get(`/combustibles/${id}/historial`);
        return response.data
    },
}

//Servicio de Proveedores
export const proveedoresService = {
    listar: async () => {
        const response = await api.get('/proveedores');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/proveedores/activos');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/proveedores/${id}`);
        return response.data;
    },
    crear: async (proveedor) => {
        const response = await api.post('/proveedores', proveedor);
        return response.data;
    },
    actualizar: async (id, proveedor) => {
        const response = await api.put(`/proveedores/${id}`, proveedor);
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/proveedores/${id}/toggle`);
        return response.data;
    },
    eliminar: async (id) => {
        const response = await api.delete(`/proveedores/${id}`);
        return response.data;
    }
};

//Servicio de Compras
export const comprasService = {
    listar: async () => {
        const response = await api.get('/compras');
        return response.data;
    },
    listarPorProveedor: async (proveedorId) => {
        const response = await api.get(`/compras/proveedor/${proveedorId}`);
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/compras/${id}`);
        return response.data;
    },
    registrar: async (compra) => {
        const response = await api.post('/compras', compra);
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/compras/${id}/toggle`);
        return response.data;
    },
    reporteMensual: async (anio, mes) => {
        const response = await api.get('/compras/reporte',{params: {anio, mes}});
        return response.data;
    },
    pendientesCarga: async () => {
        const response = await api.get('/compras/pendientes-carga');
        return response.data;
    },
    descargarPipa: async (datos) => {
        const response = await api.post('/compras/descargar-pipa', datos);
        return response.data;
    },
};

//Servicio de Tanques
export const tanquesService = {
    listar: async () => {
        const response = await api.get('/tanques');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/tanques/activos');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/tanques/${id}`);
        return response.data;
    },
    crear: async (tanque) => {
        const response = await api.post('/tanques', tanque);
        return response.data;
    },
    actualizar: async (id, tanque) => {
        const response = await api.put(`/tanques/${id}`, tanque);
        return response.data;
    },
    cargar: async (id, litros) => {
        const response = await api.post(`/tanques/${id}/cargar`, null, {params: {litros}});
        return response.data;
    },
    descargar: async (id, litros) => {
        const response = await api.post(`/tanques/${id}/descargar`, null, {params: {litros}});
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/tanques/${id}/toggle`);
        return response.data;
    },
};
