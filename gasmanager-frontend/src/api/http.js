import axios from "axios";

// Limpia "" a null en JSON de salida: los campos opcionales vacíos
// (selects, números, fechas, enums) romperían al backend con 400/500.
// No toca: 0, false, null, blobs ni FormData.
export const sanear = (valor) => {
    if (Array.isArray(valor)) return valor.map(sanear);
    if (valor && typeof valor === 'object' && !(valor instanceof Blob) && !(valor instanceof FormData)) {
        const limpio = {};
        for (const [k, v] of Object.entries(valor)) {
            limpio[k] = v === '' ? null : sanear(v);
        }
        return limpio;
    }
    return valor;
};

export const crearApi = () => {
    const api = axios.create({
        baseURL: '/api',
        headers: {'Content-Type': 'application/json'}
    });

    api.interceptors.request.use(config => {
        const token = localStorage.getItem('token');
        if (token) config.headers.Authorization = `Bearer ${token}`;
        if (config.data && !(config.data instanceof FormData)) {
            const ct = String(config.headers['Content-Type'] || '');
            if (ct.includes('application/json')) {
                try {
                    config.data = JSON.stringify(sanear(JSON.parse(config.data)));
                } catch {
                    // si no es JSON parseable, se deja intacto
                }
            } else if (typeof config.data === 'object') {
                config.data = sanear(config.data);
            }
        }
        return config;
    });

    return api;
};
