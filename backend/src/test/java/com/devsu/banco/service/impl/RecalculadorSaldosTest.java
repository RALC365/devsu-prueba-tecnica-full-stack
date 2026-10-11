package com.devsu.banco.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.exception.SaldoNoDisponibleException;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.support.Fixtures;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecalculadorSaldosTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2022, 2, 10, 9, 0);

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private RecalculadorSaldos recalculador;

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cuenta = Fixtures.cuenta(1L, "225487", "100.00", Fixtures.cliente(2L, "Marianela Montalvo"));
    }

    @Test
    void recalculaElSaldoDeCadaMovimientoEnOrdenCronologico() {
        Movimiento deposito = Fixtures.movimiento(1L, cuenta, TipoMovimiento.CREDITO, "600.00", "0", FECHA);
        Movimiento retiro = Fixtures.movimiento(2L, cuenta, TipoMovimiento.DEBITO, "-200.00", "0", FECHA.plusHours(1));
        when(movimientoRepository.findByCuentaIdOrderByIdAsc(1L)).thenReturn(List.of(deposito, retiro));

        recalculador.recalcular(cuenta);

        assertThat(deposito.getSaldo()).isEqualByComparingTo("700.00");
        assertThat(retiro.getSaldo()).isEqualByComparingTo("500.00");
        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("500.00");
    }

    @Test
    void sinMovimientosElSaldoDisponibleEsElSaldoInicial() {
        cuenta.setSaldoInicial(new BigDecimal("250.00"));
        when(movimientoRepository.findByCuentaIdOrderByIdAsc(1L)).thenReturn(List.of());

        recalculador.recalcular(cuenta);

        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("250.00");
    }

    @Test
    void rechazaElHistorialSiAlgunSaldoSuperaElMaximoRepresentable() {
        cuenta.setSaldoInicial(new BigDecimal("9999999999999.00"));
        Movimiento deposito = Fixtures.movimiento(1L, cuenta, TipoMovimiento.CREDITO, "1.00", "0", FECHA);
        when(movimientoRepository.findByCuentaIdOrderByIdAsc(1L)).thenReturn(List.of(deposito));

        assertThatThrownBy(() -> recalculador.recalcular(cuenta))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El saldo resultante supera el máximo permitido");
    }

    @Test
    void rechazaElHistorialSiAlgunSaldoQuedaNegativo() {
        Movimiento retiro = Fixtures.movimiento(1L, cuenta, TipoMovimiento.DEBITO, "-150.00", "0", FECHA);
        when(movimientoRepository.findByCuentaIdOrderByIdAsc(1L)).thenReturn(List.of(retiro));

        assertThatThrownBy(() -> recalculador.recalcular(cuenta))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
    }
}
