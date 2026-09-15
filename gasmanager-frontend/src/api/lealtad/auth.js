import {crearApi} from "../http.js";

const api = crearApi();

// Servicio de programa de lealtad (se activa desde el módulo)
export const programaService = {
    listar: async () => {
        const response = await api.get('/lealtad/programas');
        return response.data;
    },
    activo: async () => {
        const response = await api.get('/lealtad/programas/activo');
        return response.data;
    },
    crear: async (programa) => {
        const response = await api.post('/lealtad/programas', programa);
        return response.data;
    },
    actualizar: async (id, programa) => {
        const response = await api.put(`/lealtad/programas/${id}`, programa);
        return response.data;
    },
    activar: async (id) => {
        const response = await api.put(`/lealtad/programas/${id}/activar`);
        return response.data;
    },
    desactivar: async (id) => {
        const response = await api.put(`/lealtad/programas/${id}/desactivar`);
        return response.data;
    },
};

// Servicio de puntos por ticket (0 puntos si no hay programa vigente: no es error)
export const lealtadService = {
    acumular: async (ventaId) => {
        const response = await api.post(`/lealtad/transacciones/venta/${ventaId}`);
        return response.data;
    },
    cuentas: async () => {
        const response = await api.get('/lealtad/cuentas');
        return response.data;
    },
    cuentaPorVenta: async (ventaId) => {
        const response = await api.get(`/lealtad/cuentas/venta/${ventaId}`);
        return response.data;
    },
};
