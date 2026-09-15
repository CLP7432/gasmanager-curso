import {crearApi} from "../http.js";

const api = crearApi();

//Servicio de Clientes
export const clientesService = {
    listar: async () => {
        const response = await api.get('/clientes');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/clientes/activos');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/clientes/${id}`);
        return response.data;
    },
    obtenerPorRFC: async (rfc) => {
        const response = await api.get(`/clientes/rfc/${rfc}`);
        return response.data;
    },
    buscarPorRazonSocial: async (razonSocial) => {
        const response = await api.get(`/clientes/buscar?razonSocial=${razonSocial}`);
        return response.data;
    },
    crear: async (cliente) => {
        const response = await api.post('/clientes', cliente);
        return response.data;
    },
    actualizar: async (id, cliente) => {
        const response = await api.put(`/clientes/${id}`, cliente);
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/clientes/${id}/toggle`);
        return response.data;
    },
    eliminar: async (id) => {
        const response = await api.delete(`/clientes/${id}`);
        return response.data;
    }
};

//Servicio de Créditos
export const creditosService = {
    listarTodos: async () => {
        const response = await api.get('/creditos');
        return response.data;
    },
    listarPorCliente: async (clienteId) => {
        const response = await api.get(`/creditos/cliente/${clienteId}`);
        return response.data;
    },
    listarPorEstado: async (estado) => {
        const response = await api.get(`/creditos/estado/${estado}`);
        return response.data;
    },
    listarActivosConSaldo: async () => {
        const response = await api.get('/creditos/activos-con-saldo');
        return response.data;
    },
    listarVencidos: async () => {
        const response = await api.get('/creditos/vencidos');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/creditos/${id}`);
        return response.data;
    },
    listarAbonos: async (id) => {
        const response = await api.get(`/creditos/${id}/abonos`);
        return response.data;
    },
    crear: async (credito) => {
        const response = await api.post('/creditos', credito);
        return response.data;
    },
    registrarAbono: async (creditoId, abono) => {
        const response = await api.post(`/creditos/${creditoId}/abonos`, abono);
        return response.data;
    },
    actualizar: async (id, credito) => {
        const response = await api.put(`/creditos/${id}`, credito);
        return response.data;
    },
    cancelar: async (id, motivo) => {
        const response = await api.post(`/creditos/${id}/cancelar`, null, {
            params: {motivo}
        });
        return response.data;
    }
};
// Servicio de Notas de Crédito
export const notasCreditoService = {
    listar: async () => {
        const response = await api.get('/notas-credito');
        return response.data;
    },
    listarPorCredito: async (creditoId) => {
        const response = await api.get(`/notas-credito/credito/${creditoId}`);
        return response.data;
    },
    listarPorCliente: async (clienteId) => {
        const response = await api.get(`/notas-credito/cliente/${clienteId}`);
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/notas-credito/${id}`);
        return response.data;
    },
    crear: async (nota) => {
        const response = await api.post('/notas-credito', nota);
        return response.data;
    },
    actualizar: async (id, nota) => {
        const response = await api.put(`/notas-credito/${id}`, nota);
        return response.data;
    },
    bloquear: async (id) => {
        const response = await api.patch(`/notas-credito/${id}/bloquear`);
        return response.data;
    },
    desbloquear: async (id) => {
        const response = await api.patch(`/notas-credito/${id}/desbloquear`);
        return response.data;
    },
    registrarCargas: async (id, items) => {
        const response = await api.post(`/notas-credito/${id}/cargas`, items);
        return response.data;
    },
    liquidar: async (payload) => {
        const response = await api.post('/notas-credito/liquidar', payload);
        return response.data;
    },
    listarTodosAbonos: async () => {
        const response = await api.get('/creditos/abonos');
        return response.data;
    },

};





