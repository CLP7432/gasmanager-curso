

export const getPermisos = (usuario) => {
    return usuario?.permisos || [];
};

export const tienePermiso = (usuario, codigo) => {
    if(!usuario) return false;
    if(usuario.rol === 'ADMIN') return true;
    return getPermisos(usuario).includes(codigo);
};

export const tieneCualquierPermiso = (usuario, codigos) => {
    return codigos.some(codigo => tienePermiso(usuario, codigo));
};

export const tieneTodosLosPermisos = (usuario, codigos) => {
    return codigos.every(codigo => tienePermiso(usuario, codigo));
};

