package com.devsu.banco.dto.cuenta;

import com.devsu.banco.entity.TipoCuenta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CuentaPatchRequest(
        @Pattern(regexp = ValidacionCuenta.NUMERO_CUENTA, message = ValidacionCuenta.NUMERO_CUENTA_MENSAJE)
        String numeroCuenta,

        TipoCuenta tipoCuenta,

        @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = ValidacionCuenta.MONTO_MENSAJE)
        BigDecimal saldoInicial,

        Boolean estado,

        Long clienteId) {
}
