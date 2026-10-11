package com.devsu.banco.exception;

public class SaldoNoDisponibleException extends ReglaNegocioException {

    public static final String MENSAJE = "Saldo no disponible";

    public SaldoNoDisponibleException() {
        super(CodigoError.SALDO_NO_DISPONIBLE, MENSAJE);
    }
}
