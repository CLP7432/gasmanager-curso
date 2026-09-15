package com.gasmanager.inventarios.enums;

public enum TipoProveedor {
    COMBUSTIBLE, ACEITES, VARIOS;

    public static TipoProveedor fromString(String valor) {
        if (valor == null) return null;
        try {
            return TipoProveedor.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}