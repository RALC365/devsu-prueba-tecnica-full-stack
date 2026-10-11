package com.devsu.banco.exception;

public class ValorInvalidoException extends IllegalArgumentException {

    public ValorInvalidoException(String mensaje) {
        super(mensaje);
    }
}
