package com.devsu.banco.entity;

import com.devsu.banco.util.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Set;

public enum TipoCuenta {
    AHORROS("AHORRO"),
    CORRIENTE;

    private final Set<String> alias;

    TipoCuenta(String... alias) {
        this.alias = Set.of(alias);
    }

    @JsonCreator
    public static TipoCuenta desde(String valor) {
        return EnumParser.parse(TipoCuenta.class, valor, tipo -> tipo.alias);
    }
}
