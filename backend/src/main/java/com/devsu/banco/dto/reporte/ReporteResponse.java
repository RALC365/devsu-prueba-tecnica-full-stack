package com.devsu.banco.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReporteResponse(
        Long clienteId,
        String cliente,
        String identificacion,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDateTime fechaGeneracion,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        List<ReporteCuentaResponse> cuentas,
        List<ReporteMovimientoResponse> movimientos) {
}
