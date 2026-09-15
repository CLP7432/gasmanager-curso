import React from "react";

const TablaDinamica = ({columnas, datos, acciones}) => {

    return (
        <div className="table-container">
            <table>
                <thead>
                    <tr>
                        {columnas.map(col => (
                            <th key={col.key}>{col.label}</th>
                        ))}
                        {acciones && <th>Acciones</th>}
                    </tr>
                </thead>
                <tbody>
                {datos.length === 0 ? (
                    <tr>
                        <td colSpan={columnas.length + (acciones ? 1 : 0)}>
                            No hay registros.
                        </td>
                    </tr>
                ) : (
                    datos.map(item => (
                        <tr key={item.id}>
                            {columnas.map(col => (
                                <td key={col.key}>
                                    {col.render ? col.render(item) : (item[col.key] ?? '-')}
                                </td>
                            ))}
                            {acciones && (
                                <td>{acciones(item)}</td>
                            )}
                        </tr>
                    ))
                )}
                </tbody>
            </table>
        </div>
    );
}
export default TablaDinamica;