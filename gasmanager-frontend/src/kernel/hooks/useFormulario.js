import {useState} from "react";

export const useFormulario = (servicio, objetoInicial, mensajeOk = 'Guardado exitosamente', recargar, fnGuardar) => {
    const [mostrarForm, setMostrarForm] = useState(false);
    const [editandoId, setEditandoId] = useState(null);
    const [objeto, setObjeto] = useState({...objetoInicial});

    const handleNuevo = () => {
      setEditandoId(null);
      setObjeto({...objetoInicial});
      setMostrarForm(true);
    };

    const handleEditar = (item) => {
        setEditandoId(item.id);
        setObjeto({...objetoInicial, ...item});
        setMostrarForm(true);
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        setObjeto(prev => ({...prev, [name]: value}));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try{
            if (fnGuardar) {
                await fnGuardar(objeto, editandoId);
            } else if (editandoId) {
                await servicio.actualizar(editandoId, objeto);
            } else {
                await servicio.crear(objeto);
            }
            alert(mensajeOk);
            cerrar();
            if(recargar) recargar();
        }catch (error){
            alert(error.response?.data?.message || 'Error al guardar');
        }
    };
    const cerrar = () => {
        setMostrarForm(false);
        setEditandoId(null);
        setObjeto({...objetoInicial});
    };

    return {mostrarForm, editandoId, objeto, setObjeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar};
}