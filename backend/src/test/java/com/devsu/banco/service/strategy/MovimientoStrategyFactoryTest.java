package com.devsu.banco.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.support.Fixtures;
import java.util.List;
import org.junit.jupiter.api.Test;

class MovimientoStrategyFactoryTest {

    private final CreditoStrategy credito = new CreditoStrategy();
    private final DebitoStrategy debito = new DebitoStrategy(null, Fixtures.propiedades("1000"));

    @Test
    void resuelveLaEstrategiaDeCadaTipo() {
        MovimientoStrategyFactory factory = new MovimientoStrategyFactory(List.of(debito, credito));

        assertThat(factory.obtener(TipoMovimiento.CREDITO)).isSameAs(credito);
        assertThat(factory.obtener(TipoMovimiento.DEBITO)).isSameAs(debito);
    }

    @Test
    void fallaAlIniciarSiUnTipoNoTieneEstrategia() {
        List<MovimientoStrategy> incompletas = List.of(credito);

        assertThatThrownBy(() -> new MovimientoStrategyFactory(incompletas))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DEBITO");
    }

    @Test
    void fallaAlIniciarSiHayEstrategiasDuplicadas() {
        List<MovimientoStrategy> duplicadas = List.of(credito, new CreditoStrategy(), debito);

        assertThatThrownBy(() -> new MovimientoStrategyFactory(duplicadas))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duplicada");
    }
}
