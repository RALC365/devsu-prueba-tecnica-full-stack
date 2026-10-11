package com.devsu.banco.util;

import com.devsu.banco.exception.ValorInvalidoException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// ignora mayúsculas y tildes, y acepta alias como "Depósito" o "Ahorro"
public final class EnumParser {

    private EnumParser() {
    }

    public static <E extends Enum<E>> E parse(Class<E> tipo, String valor, Function<E, Set<String>> alias) {
        String normalizado = normalizar(valor);
        return Arrays.stream(tipo.getEnumConstants())
                .filter(constante -> constante.name().equals(normalizado)
                        || alias.apply(constante).contains(normalizado))
                .findFirst()
                .orElseThrow(() -> new ValorInvalidoException("'%s' no es un valor válido. Valores permitidos: %s"
                        .formatted(valor, valoresPermitidos(tipo))));
    }

    static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinAcentos.toUpperCase(Locale.ROOT);
    }

    private static <E extends Enum<E>> String valoresPermitidos(Class<E> tipo) {
        return Arrays.stream(tipo.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
