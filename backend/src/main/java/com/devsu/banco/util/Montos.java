package com.devsu.banco.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Montos {

    public static final int ESCALA = 2;
    public static final BigDecimal MAXIMO = new BigDecimal("9999999999999.99");
    public static final String MAXIMO_EXCEDIDO = "El saldo resultante supera el máximo permitido";

    private Montos() {
    }

    public static BigDecimal normalizar(BigDecimal monto) {
        return monto.setScale(ESCALA, RoundingMode.HALF_UP);
    }
}
