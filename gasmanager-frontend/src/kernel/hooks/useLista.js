import {useEffect, useState, useCallback} from "react";

export const useLista = (servicio, metodo = 'listar', dependencias = []) => {

    const [datos, setDatos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cargar = useCallback(async () => {
        setLoading(true);
        try{
            const data = await servicio[metodo]();
            setDatos(data);
            setError(null);
        }catch (err){
            console.error(`Error al cargar ${metodo}: `, err);
            setError(err);
        }
        setLoading(false);
    }, [servicio, metodo]);

    useEffect(() => {
        cargar();
    }, [...dependencias, cargar]);

    return {datos, setDatos, loading, error, cargar};
}