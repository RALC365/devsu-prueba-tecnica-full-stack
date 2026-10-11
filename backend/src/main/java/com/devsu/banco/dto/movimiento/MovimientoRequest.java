package com.devsu.banco.dto.movimiento;

import com.devsu.banco.entity.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record MovimientoRequest(
        @Schema(example = "478758")
        @NotBlank(message = "El número de cuenta es obligatorio")
        String numeroCuenta,

        @Schema(example = "DEBITO", description = "CREDITO o DEBITO")
        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipoMovimiento,

        @Schema(example = "575")
        @NotNull(message = "El valor es obligatorio")
        @Positive(message = "El valor debe ser mayor a 0")
        @Digits(integer = 13, fraction = 2, message = "El valor admite hasta 13 enteros y 2 decimales")
        BigDecimal valor) {
}
