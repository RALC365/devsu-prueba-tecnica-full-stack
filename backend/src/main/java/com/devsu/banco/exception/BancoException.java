package com.devsu.banco.exception;

import lombok.Getter;

@Getter
public abstract class BancoException extends RuntimeException {

    private final CodigoError codigo;

    protected BancoException(CodigoError codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
}
