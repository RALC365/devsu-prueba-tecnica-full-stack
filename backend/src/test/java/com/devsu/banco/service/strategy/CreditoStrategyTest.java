package com.devsu.banco.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.support.Fixtures;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CreditoStrategyTest {

    private static final LocalDate HOY = LocalDate.of(2022, 2, 10);

    private final CreditoStrategy estrategia = new CreditoStrategy();

    @Test
    void unDepositoNoTieneRestriccionesDeSaldoNiDeCupo() {
        Cuenta sinSaldo = Fixtures.cuenta(1L, "495878", "0.00", Fixtures.cliente(1L, "Juan Osorio"));

        assertThatCode(() -> estrategia.validar(OperacionMovimiento.nueva(sinSaldo, new BigDecimal("150"), HOY)))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaUnDepositoQueDejariaElSaldoFueraDeRango() {
        Cuenta casiLlena = Fixtures.cuenta(1L, "495878", "9999999999999.00", Fixtures.cliente(1L, "Juan Osorio"));

        assertThatThrownBy(() -> estrategia.validar(OperacionMovimiento.nueva(casiLlena, new BigDecimal("1.00"), HOY)))
                .isInstanceOfSatisfying(ReglaNegocioException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo(CodigoError.OPERACION_NO_PERMITIDA));
    }

    @Test
    void registraElValorConSignoPositivo() {
        assertThat(estrategia.aplicarSigno(new BigDecimal("600"))).isEqualByComparingTo("600");
        assertThat(estrategia.aplicarSigno(new BigDecimal("-600"))).isEqualByComparingTo("600");
        assertThat(estrategia.getTipo()).isEqualTo(TipoMovimiento.CREDITO);
    }
}
