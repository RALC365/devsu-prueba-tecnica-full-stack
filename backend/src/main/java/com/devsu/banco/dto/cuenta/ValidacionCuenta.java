package com.devsu.banco.dto.cuenta;

final class ValidacionCuenta {

    static final String NUMERO_CUENTA = "^[0-9]{6,20}$";
    static final String NUMERO_CUENTA_MENSAJE = "El número de cuenta debe tener entre 6 y 20 dígitos";
    static final String MONTO_MENSAJE = "El saldo inicial admite hasta 13 enteros y 2 decimales";

    private ValidacionCuenta() {
    }
}
