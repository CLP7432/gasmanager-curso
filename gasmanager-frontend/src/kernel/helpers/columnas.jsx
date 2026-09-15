import React from "react";

export const idCol = {key: 'id', label: 'ID'};

export const estadoCol = (getValor, activoLabel = 'Activo', inactivoLabel = 'Inactivo') => ({
    key: 'estado',
    label: 'Estado',
    render: (item) => getValor(item)
        ? <span className="badge badge-success">{activoLabel}</span>
        : <span className="badge badge-danger">{inactivoLabel}</span>
});
export const columna = (key, label, render) => render ? {key, label, render} : {key, label};