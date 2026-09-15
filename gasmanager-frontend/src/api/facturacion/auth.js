import {crearApi} from "../http.js";

const api = crearApi();

// Servicio de Facturas (CFDI sin timbrado: genera XML/PDF locales)
export const facturasService = {
    listar: async () => {
        const response = await api.get('/facturas');
        return response.data;
    },
    listarPorCliente: async (clienteFiscalId) => {
        const response = await api.get(`/facturas/cliente/${clienteFiscalId}`);
        return response.data;
    },
    obtener: async (id) => {
        const response = await api.get(`/facturas/${id}`);
        return response.data;
    },
    disponibles: async (clienteId) => {
        const response = await api.get(`/facturas/disponibles/${clienteId}`);
        return response.data;
    },
    crear: async (factura) => {
        const response = await api.post('/facturas', factura);
        return response.data;
    },
    cancelar: async (id, motivo) => {
        const response = await api.post(`/facturas/${id}/cancelar`, null, {params: {motivo}});
        return response.data;
    },
    xml: async (id) => {
        const response = await api.get(`/facturas/${id}/xml`, {responseType: 'blob'});
        return response.data;
    },
    pdf: async (id) => {
        const response = await api.get(`/facturas/${id}/pdf`, {responseType: 'blob'});
        return response.data;
    },
    enviarCorreo: async (id) => {
        const response = await api.post(`/facturas/${id}/enviar-correo`);
        return response.data;
    },
    emisor: async () => {
        const response = await api.get('/facturas/emisor');
        return response.data;
    },
};

// Servicio de Clientes Fiscales (RFC, régimen fiscal y domicilio del receptor)
export const clientesFiscalesService = {
    listar: async () => {
        const response = await api.get('/clientes-fiscales');
        return response.data;
    },
    obtener: async (id) => {
        const response = await api.get(`/clientes-fiscales/${id}`);
        return response.data;
    },
    obtenerPorCliente: async (clienteId) => {
        const response = await api.get(`/clientes-fiscales/cliente/${clienteId}`);
        return response.data;
    },
    crear: async (cliente) => {
        const response = await api.post('/clientes-fiscales', cliente);
        return response.data;
    },
    actualizar: async (id, cliente) => {
        const response = await api.put(`/clientes-fiscales/${id}`, cliente);
        return response.data;
    },
    eliminar: async (id) => {
        const response = await api.delete(`/clientes-fiscales/${id}`);
        return response.data;
    },
};
