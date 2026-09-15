import {crearApi} from "../http.js";

const api = crearApi();

//Servicio de Empleados
export const empleadosService = {
    listar: async () => {
        const response = await api.get('/empleados');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/empleados/activos');
        return response.data;
    },
    listarDespachadores: async () => {
        const response = await api.get('/empleados/despachadores');
        return response.data;
    },
    listarPorPuesto: async (puestoId) => {
        const response = await api.get(`/empleados/puesto/${puestoId}`);
        return response.data;
    },
    listarPorDepartamento: async (departamentoId) => {
        const response = await api.get(`/empleados/departamento/${departamentoId}`);
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/empleados/${id}`);
        return response.data;
    },
    crear: async (empleado) => {
        const response = await api.post('/empleados', empleado);
        return response.data;
    },
    actualizar: async (id, empleado) => {
        const response = await api.put(`/empleados/${id}`, empleado);
        return response.data;
    },
    desactivar: async (id, fechaBaja, motivo) => {
        const response = await api.patch(`/empleados/${id}/desactivar`, null, {params: {fechaBaja, motivo}});
        return response.data;
    },
    reactivar: async (id) => {
        const response = await api.patch(`/empleados/${id}/reactivar`);
        return response.data;
    }
};

//Servicio de Puestos
export const puestosService = {
    listar: async () => {
        const response = await api.get('/puestos');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/puestos/activos');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/puestos/${id}`);
        return response.data;
    },
    crear: async (puesto) => {
        const response = await api.post('/puestos', puesto);
        return response.data;
    },
    actualizar: async (id, puesto) => {
        const response = await api.put(`/puestos/${id}`, puesto);
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/puestos/${id}/toggle`);
        return response.data;
    },
    eliminar: async (id) => {
        const response = await api.delete(`/puestos/${id}`);
        return response.data;
    }
};

//Servicio de Departamentos
export const departamentosService = {
    listar: async () => {
        const response = await api.get('/departamentos');
        return response.data;
    },
    listarActivos: async () => {
        const response = await api.get('/departamentos/activos');
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/departamentos/${id}`);
        return response.data;
    },
    crear: async (departamento) => {
        const response = await api.post('/departamentos', departamento);
        return response.data;
    },
    actualizar: async (id, departamento) => {
        const response = await api.put(`/departamentos/${id}`, departamento);
        return response.data;
    },
    toggleActivo: async (id) => {
        const response = await api.patch(`/departamentos/${id}/toggle`);
        return response.data;
    },
    eliminar: async (id) => {
        const response = await api.delete(`/departamentos/${id}`);
        return response.data;
    }
};

//Servicio de Incidencias
export const incidenciasService = {
    listar: async () => {
        const response = await api.get('/incidencias');
        return response.data;
    },
    listarPorEmpleado: async (empleadoId) => {
        const response = await api.get(`/incidencias/empleado/${empleadoId}`);
        return response.data;
    },
    listarPorEmpleadoYPeriodo: async (empleadoId, inicio, fin) => {
        const response = await api.get(`/incidencias/empleado/${empleadoId}/periodo`, {params: {inicio, fin}});
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/incidencias/${id}`);
        return response.data;
    },
    crear: async (incidencia) => {
        const response = await api.post('/incidencias', incidencia);
        return response.data;
    },
    actualizar: async (id, incidencia) => {
        const response = await api.put(`/incidencias/${id}`, incidencia);
        return response.data;
    },
    eliminar: async (id) => {
        const response = await api.delete(`/incidencias/${id}`);
        return response.data;
    }
};

//Servicio de Nóminas
export const nominasService = {
    listar: async () => {
        const response = await api.get('/nominas');
        return response.data;
    },
    listarPorEstado: async (estado) => {
        const response = await api.get(`/nominas/estado/${estado}`);
        return response.data;
    },
    obtenerPorId: async (id) => {
        const response = await api.get(`/nominas/${id}`);
        return response.data;
    },
    procesar: async (request) => {
        const response = await api.post('/nominas/procesar', request);
        return response.data;
    },
    marcarPagada: async (id) => {
        const response = await api.post(`/nominas/${id}/marcar-pagada`);
        return response.data;
    },
    cancelar: async (id, motivo) => {
        const response = await api.post(`/nominas/${id}/cancelar`, null, {params: {motivo}});
        return response.data;
    }
};