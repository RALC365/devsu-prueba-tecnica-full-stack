package com.devsu.banco.dto.reporte;

import com.devsu.banco.entity.TipoCuenta;
import java.math.BigDecimal;

public record ReporteCuentaResponse(
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoDisponible,
        Boolean estado,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos) {
}
