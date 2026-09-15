import {useAuth} from "../../contexts/AuthContext.jsx";
import {tieneCualquierPermiso, tienePermiso, tieneTodosLosPermisos} from "../helpers/permisos.js";

export const usePermisos = () => {
    const {user} = useAuth();

    const puede = (codigo) => tienePermiso(user,codigo);
    const puedeAlguno = (codigos) => tieneCualquierPermiso(user, codigos);
    const puedeTodos = (codigos) => tieneTodosLosPermisos(user, codigos);

    return {puede, puedeAlguno, puedeTodos};
}