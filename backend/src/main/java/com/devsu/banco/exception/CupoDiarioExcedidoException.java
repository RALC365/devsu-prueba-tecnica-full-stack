package com.devsu.banco.exception;

public class CupoDiarioExcedidoException extends ReglaNegocioException {

    public static final String MENSAJE = "Cupo diario Excedido";

    public CupoDiarioExcedidoException() {
        super(CodigoError.CUPO_DIARIO_EXCEDIDO, MENSAJE);
    }
}
