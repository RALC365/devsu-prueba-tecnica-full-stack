package com.devsu.banco.exception;

public class SolicitudInvalidaException extends BancoException {

    public SolicitudInvalidaException(String mensaje) {
        super(CodigoError.SOLICITUD_INVALIDA, mensaje);
    }
}
