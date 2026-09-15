import {crearApi} from "../http.js";

const api = crearApi();

//Servicio de Ventas
export const ventasService = {
  listar: async () => {
      const response = await api.get('/ventas');
      return response.data;
  },
    obtenerPorId: async (id) => {
      const response = await api.get(`/ventas/${id}`);
      return response.data;
    },
    obtenerPorFolio: async (folio) => {
      const response = await api.get(`/ventas/folio/${folio}`);
      return response.data;
    },
    registrar: async (venta) => {
      const response = await api.post('/ventas', venta);
      return response.data;
    },
    cancelar: async (id) => {
      const response = await api.patch(`/ventas/${id}/cancelar`);
      return response.data;
    },
    listarPorTurno: async (turnoId) => {
        const response = await api.get(`/ventas/turno/${turnoId}`);
        return response.data;
    },
    reporteMensual: async (anio, mes) => {
        const response = await api.get('/ventas/reporte', {params: {anio, mes}});
        return response.data;
    },
};
//Servicio de Turnos
export const turnosService = {
    listar: async (estado) => {
        const response = await api.get('/turnos', {params: {estado}});
        return response.data;
    },
    crear: async (turno) => {
        const response = await api.post('/turnos', turno);
        return response.data;
    },
    cerrar: async (id) => {
        const response = await api.post(`/turnos/${id}/cerrar`);
        return response.data;
    },
};
//Servicio de Dispensarios
export const dispensariosService = {
    listarCompletos: async () => {
        const response = await api.get('/dispensarios/completos');
        return response.data;
    },
    obtenerCompleto: async (id) => {
        const response = await api.get(`/dispensarios/completo/${id}`);
        return response.data;
    },
    crearCompleto: async (dispensario) => {
        const response = await api.post('/dispensarios/completo', dispensario);
        return response.data;
    },
    actualizarCompleto: async (id, dispensario) => {
        const response = await api.put(`/dispensarios/completo/${id}`, dispensario);
        return response.data;
    },
    asignarDespachador: async (id, despachadorId, despachadorNombre) => {
        const response = await api.put(`/dispensarios/${id}/despachador`, {despachadorId, despachadorNombre});
        return response.data;
    },
    cambiarActivo: async (id, activo) => {
        const response = await api.put(`/dispensarios/${id}/activo`, null, {params: {activo}});
        return response.data;
    },
};
//Servicio de Surtidores de Aceite
export const surtidoresAceiteService = {
    listar: async () => {
        const response = await api.get('/surtidores-aceite');
        return response.data;
    },
    obtener: async (id) => {
        const response = await api.get(`/surtidores-aceite/${id}`);
        return response.data;
    },
    stock: async (id) => {
        const response = await api.get(`/surtidores-aceite/${id}/stock`);
        return response.data;
    },
    guardar: async (datos) => {
        const response = await api.post('/surtidores-aceite', datos);
        return response.data;
    },
    actualizar: async (id, datos) => {
        const response = await api.put(`/surtidores-aceite/${id}`, datos);
        return response.data;
    },
    entregar: async (datos) => {
        const response = await api.post('/surtidores-aceite/entregar', datos);
        return response.data;
    },
};
//Servicio de Cortes
export const cortesService = {
    listar: async (estado) => {
        const response = await api.get('/cortes', {params: {estado}});
        return response.data;
    },
    listarPorTurno: async (turnoId) => {
        const response = await api.get(`/cortes/turno/${turnoId}`);
        return response.data;
    },
    obtener: async (id) => {
        const response = await api.get(`/cortes/${id}`);
        return response.data;
    },
    resumen: async (turnoId, dispensarioId) => {
        const response = await api.get('/cortes/resumen', {params: {turnoId, dispensarioId}});
        return response.data;
    },
    generar: async (datos) => {
        const response = await api.post('/cortes/generar', datos);
        return response.data;
    },
    validar: async (id, autorizadoPor) => {
        const response = await api.post(`/cortes/${id}/validar`, null, {params: {autorizadoPor}});
        return response.data;
    },
    cerrar: async (id) => {
        const response = await api.post(`/cortes/${id}/cerrar`);
        return response.data;
    },
    actualizar: async (id, datos) => {
        const response = await api.put(`/cortes/${id}`, datos);
        return response.data;
    },
};