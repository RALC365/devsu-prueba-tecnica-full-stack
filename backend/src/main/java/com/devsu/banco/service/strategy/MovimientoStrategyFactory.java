package com.devsu.banco.service.strategy;

import com.devsu.banco.entity.TipoMovimiento;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MovimientoStrategyFactory {

    private final Map<TipoMovimiento, MovimientoStrategy> estrategias;

    public MovimientoStrategyFactory(List<MovimientoStrategy> estrategias) {
        this.estrategias = estrategias.stream().collect(Collectors.toMap(
                MovimientoStrategy::getTipo,
                Function.identity(),
                (primera, segunda) -> {
                    throw new IllegalStateException("Estrategia duplicada para " + primera.getTipo());
                },
                () -> new EnumMap<>(TipoMovimiento.class)));
        Arrays.stream(TipoMovimiento.values())
                .filter(tipo -> !this.estrategias.containsKey(tipo))
                .findAny()
                .ifPresent(tipo -> {
                    throw new IllegalStateException("No existe una estrategia para " + tipo);
                });
    }

    public MovimientoStrategy obtener(TipoMovimiento tipo) {
        return estrategias.get(tipo);
    }
}
