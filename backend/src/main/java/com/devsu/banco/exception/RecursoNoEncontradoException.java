package com.devsu.banco.exception;

public class RecursoNoEncontradoException extends BancoException {

    public RecursoNoEncontradoException(String mensaje) {
        super(CodigoError.RECURSO_NO_ENCONTRADO, mensaje);
    }
}
