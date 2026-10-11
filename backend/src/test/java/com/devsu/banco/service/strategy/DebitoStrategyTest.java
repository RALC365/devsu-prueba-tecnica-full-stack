package com.devsu.banco.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.CupoDiarioExcedidoException;
import com.devsu.banco.exception.SaldoNoDisponibleException;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.support.Fixtures;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebitoStrategyTest {

    private static final LocalDate HOY = LocalDate.of(2022, 2, 10);

    @Mock
    private MovimientoRepository movimientoRepository;

    private DebitoStrategy estrategia;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        estrategia = new DebitoStrategy(movimientoRepository, Fixtures.propiedades("1000"));
        cuenta = Fixtures.cuenta(1L, "478758", "2000.00", Fixtures.cliente(1L, "Jose Lema"));
    }

    @Test
    void rechazaElDebitoCuandoElSaldoEsCero() {
        Cuenta sinSaldo = Fixtures.cuenta(2L, "495878", "0.00", Fixtures.cliente(3L, "Juan Osorio"));

        assertThatThrownBy(() -> estrategia.validar(OperacionMovimiento.nueva(sinSaldo, monto("10"), HOY)))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
        verifyNoInteractions(movimientoRepository);
    }

    @Test
    void rechazaElDebitoMayorAlSaldoDisponible() {
        assertThatThrownBy(() -> estrategia.validar(OperacionMovimiento.nueva(cuenta, monto("2000.01"), HOY)))
                .isInstanceOf(SaldoNoDisponibleException.class);
    }

    @Test
    void permiteRetirarHastaElLimiteDiarioExacto() {
        retirosDelDia("-600.00");

        assertThatCode(() -> estrategia.validar(OperacionMovimiento.nueva(cuenta, monto("400"), HOY)))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaElDebitoQueSuperaElCupoDiario() {
        retirosDelDia("-575.00");

        assertThatThrownBy(() -> estrategia.validar(OperacionMovimiento.nueva(cuenta, monto("500"), HOY)))
                .isInstanceOf(CupoDiarioExcedidoException.class)
                .hasMessage("Cupo diario Excedido");
    }

    @Test
    void rechazaCuandoElCupoYaFueConsumido() {
        retirosDelDia("-1000.00");

        assertThatThrownBy(() -> estrategia.validar(OperacionMovimiento.nueva(cuenta, monto("0.01"), HOY)))
                .isInstanceOf(CupoDiarioExcedidoException.class);
    }

    @Test
    void alCorregirNoCuentaElPropioMovimientoEnElCupo() {
        when(movimientoRepository.sumarValores(eq(1L), eq(TipoMovimiento.DEBITO), any(), any(), eq(7L)))
                .thenReturn(BigDecimal.ZERO);
        OperacionMovimiento correccion = new OperacionMovimiento(cuenta, monto("900"), monto("2000"), HOY, 7L);

        assertThatCode(() -> estrategia.validar(correccion)).doesNotThrowAnyException();
        verify(movimientoRepository).sumarValores(1L, TipoMovimiento.DEBITO,
                HOY.atStartOfDay(), HOY.plusDays(1).atStartOfDay(), 7L);
    }

    @Test
    void registraElValorConSignoNegativo() {
        assertThat(estrategia.aplicarSigno(monto("575"))).isEqualByComparingTo("-575");
        assertThat(estrategia.getTipo()).isEqualTo(TipoMovimiento.DEBITO);
    }

    private void retirosDelDia(String total) {
        when(movimientoRepository.sumarValores(eq(1L), eq(TipoMovimiento.DEBITO),
                eq(HOY.atStartOfDay()), eq(HOY.plusDays(1).atStartOfDay()), isNull()))
                .thenReturn(new BigDecimal(total));
    }

    private static BigDecimal monto(String valor) {
        return new BigDecimal(valor);
    }
}
