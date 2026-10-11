package com.devsu.banco.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "banco")
public record BancoProperties(
        @NotNull ZoneId zonaHoraria,
        @Valid @NotNull Movimientos movimientos,
        @Valid @NotNull Cors cors) {

    public record Movimientos(@NotNull @Positive BigDecimal limiteDiarioRetiro) {
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }
}
