package com.devsu.banco.dto.movimiento;

import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
        Long movimientoId,
        LocalDateTime fecha,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        TipoMovimiento tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo,
        Long clienteId,
        String cliente) {
}
