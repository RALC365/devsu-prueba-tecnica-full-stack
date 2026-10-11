package com.devsu.banco.exception;

public class RecursoDuplicadoException extends BancoException {

    public RecursoDuplicadoException(String mensaje) {
        super(CodigoError.RECURSO_DUPLICADO, mensaje);
    }
}
