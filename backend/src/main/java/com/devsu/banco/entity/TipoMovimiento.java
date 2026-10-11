package com.devsu.banco.entity;

import com.devsu.banco.util.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Set;

public enum TipoMovimiento {
    CREDITO("DEPOSITO"),
    DEBITO("RETIRO");

    private final Set<String> alias;

    TipoMovimiento(String... alias) {
        this.alias = Set.of(alias);
    }

    @JsonCreator
    public static TipoMovimiento desde(String valor) {
        return EnumParser.parse(TipoMovimiento.class, valor, tipo -> tipo.alias);
    }
}
