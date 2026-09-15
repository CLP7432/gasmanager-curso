import React from "react";
import CardBase from "./CardBase.jsx";

const CardDatos = ({titulo, objeto, campos, badges = {}}) => {
    return (
        <CardBase titulo={titulo}>
            {campos.map(({label, campo}) => {
                const valor = objeto[campo];
                if(badges[campo]){
                    const tipo = badges[campo](valor);
                    return(
                        <p key={campo}>
                            <strong>{label}:</strong><span className={tipo}>{valor ?? '-'}</span>
                        </p>
                    );
                }
                return (
                    <p key={campo}>
                        <strong>{label}:</strong> {valor ?? '-'}
                    </p>
                );
            })}
        </CardBase>
    );
}
export default CardDatos;