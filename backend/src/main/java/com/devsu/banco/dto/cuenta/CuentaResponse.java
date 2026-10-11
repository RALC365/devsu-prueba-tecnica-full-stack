package com.devsu.banco.dto.cuenta;

import com.devsu.banco.entity.TipoCuenta;
import java.math.BigDecimal;

public record CuentaResponse(
        Long cuentaId,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoDisponible,
        Boolean estado,
        Long clienteId,
        String cliente) {
}
