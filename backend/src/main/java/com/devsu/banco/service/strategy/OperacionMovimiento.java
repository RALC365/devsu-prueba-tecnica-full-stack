package com.devsu.banco.service.strategy;

import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OperacionMovimiento(
        Cuenta cuenta,
        BigDecimal monto,
        BigDecimal saldoPrevio,
        LocalDate dia,
        Long movimientoExcluidoId) {

    public static OperacionMovimiento nueva(Cuenta cuenta, BigDecimal monto, LocalDate dia) {
        return new OperacionMovimiento(cuenta, monto, cuenta.getSaldoDisponible(), dia, null);
    }

    public static OperacionMovimiento correccion(Movimiento movimiento, BigDecimal monto) {
        return new OperacionMovimiento(
                movimiento.getCuenta(),
                monto,
                movimiento.getSaldoAnterior(),
                movimiento.getFecha().toLocalDate(),
                movimiento.getId());
    }
}
