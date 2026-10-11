package com.devsu.banco.exception;

public class ConflictoDatosException extends BancoException {

    public ConflictoDatosException(String mensaje) {
        super(CodigoError.CONFLICTO_DE_DATOS, mensaje);
    }
}
