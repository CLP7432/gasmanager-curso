package com.gasmanager.inventarios.enums;

public enum TipoCombustible {
    MAGNA,
    PREMIUM,
    DIESEL;

    public static TipoCombustible fromString (String tipo){
        if(tipo == null) return  null;
        try{
            return TipoCombustible.valueOf(tipo.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }

}
