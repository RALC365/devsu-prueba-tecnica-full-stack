package com.devsu.banco.dto.cuenta;

import com.devsu.banco.entity.TipoCuenta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CuentaRequest(
        @Schema(example = "478758")
        @NotBlank(message = "El número de cuenta es obligatorio")
        @Pattern(regexp = ValidacionCuenta.NUMERO_CUENTA, message = ValidacionCuenta.NUMERO_CUENTA_MENSAJE)
        String numeroCuenta,

        @Schema(example = "AHORROS")
        @NotNull(message = "El tipo de cuenta es obligatorio")
        TipoCuenta tipoCuenta,

        @Schema(example = "2000")
        @NotNull(message = "El saldo inicial es obligatorio")
        @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = ValidacionCuenta.MONTO_MENSAJE)
        BigDecimal saldoInicial,

        @Schema(example = "true")
        @NotNull(message = "El estado es obligatorio")
        Boolean estado,

        @Schema(example = "1")
        @NotNull(message = "El cliente es obligatorio")
        Long clienteId) {
}
