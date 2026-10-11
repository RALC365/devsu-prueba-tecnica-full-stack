package com.devsu.banco.entity;

import com.devsu.banco.util.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Set;

public enum Genero {
    MASCULINO("M"),
    FEMENINO("F"),
    OTRO;

    private final Set<String> alias;

    Genero(String... alias) {
        this.alias = Set.of(alias);
    }

    @JsonCreator
    public static Genero desde(String valor) {
        return EnumParser.parse(Genero.class, valor, genero -> genero.alias);
    }
}
