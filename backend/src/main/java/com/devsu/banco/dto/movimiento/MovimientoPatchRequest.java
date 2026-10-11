package com.devsu.banco.dto.movimiento;

import com.devsu.banco.entity.TipoMovimiento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MovimientoPatchRequest(
        TipoMovimiento tipoMovimiento,

        @Positive(message = "El valor debe ser mayor a 0")
        @Digits(integer = 13, fraction = 2, message = "El valor admite hasta 13 enteros y 2 decimales")
        BigDecimal valor) {
}
