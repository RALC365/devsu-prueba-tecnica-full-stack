package com.devsu.banco.exception;

public class ReglaNegocioException extends BancoException {

    public ReglaNegocioException(CodigoError codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
