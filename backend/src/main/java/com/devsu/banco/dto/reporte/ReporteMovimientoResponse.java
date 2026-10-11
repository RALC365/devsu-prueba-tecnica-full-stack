package com.devsu.banco.dto.reporte;

import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteMovimientoResponse(
        LocalDateTime fecha,
        String cliente,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        Boolean estado,
        TipoMovimiento tipoMovimiento,
        BigDecimal movimiento,
        BigDecimal saldoDisponible) {
}
