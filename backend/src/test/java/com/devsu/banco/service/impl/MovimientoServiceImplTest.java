package com.devsu.banco.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devsu.banco.dto.movimiento.MovimientoPatchRequest;
import com.devsu.banco.dto.movimiento.MovimientoRequest;
import com.devsu.banco.dto.movimiento.MovimientoResponse;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.CupoDiarioExcedidoException;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.exception.SaldoNoDisponibleException;
import com.devsu.banco.mapper.MovimientoMapper;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.strategy.CreditoStrategy;
import com.devsu.banco.service.strategy.DebitoStrategy;
import com.devsu.banco.service.strategy.MovimientoStrategyFactory;
import com.devsu.banco.support.Fixtures;
import com.devsu.banco.support.RelojControlable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceImplTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2022, 2, 10, 9, 30);

    @Mock
    private MovimientoRepository movimientoRepository;
    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private RecalculadorSaldos recalculadorSaldos;

    private MovimientoServiceImpl servicio;

    @BeforeEach
    void setUp() {
        MovimientoStrategyFactory estrategias = new MovimientoStrategyFactory(List.of(
                new CreditoStrategy(),
                new DebitoStrategy(movimientoRepository, Fixtures.propiedades("1000"))));
        servicio = new MovimientoServiceImpl(movimientoRepository, cuentaRepository, estrategias,
                recalculadorSaldos, new MovimientoMapper(), new RelojControlable(AHORA, Fixtures.ZONA));
    }

    @Test
    void registraUnDepositoSumandoAlSaldoDisponible() {
        Cuenta cuenta = cuentaBloqueada("225487", "100.00");
        guardarAsignandoId(10L);

        MovimientoResponse respuesta = servicio.registrar(
                new MovimientoRequest("225487", TipoMovimiento.CREDITO, new BigDecimal("600")));

        assertThat(respuesta.movimientoId()).isEqualTo(10L);
        assertThat(respuesta.valor()).isEqualByComparingTo("600.00");
        assertThat(respuesta.saldo()).isEqualByComparingTo("700.00");
        assertThat(respuesta.fecha()).isEqualTo(AHORA);
        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("700.00");
    }

    @Test
    void registraUnRetiroConValorNegativoYDescuentaElSaldo() {
        Cuenta cuenta = cuentaBloqueada("478758", "2000.00");
        when(movimientoRepository.sumarValores(eq(1L), eq(TipoMovimiento.DEBITO), any(), any(), isNull()))
                .thenReturn(BigDecimal.ZERO);
        guardarAsignandoId(11L);

        MovimientoResponse respuesta = servicio.registrar(
                new MovimientoRequest("478758", TipoMovimiento.DEBITO, new BigDecimal("575")));

        assertThat(respuesta.valor()).isEqualByComparingTo("-575.00");
        assertThat(respuesta.saldo()).isEqualByComparingTo("1425.00");
        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("1425.00");
    }

    @Test
    void rechazaElRetiroSiNoHaySaldo() {
        cuentaBloqueada("496825", "0.00");

        assertThatThrownBy(() -> servicio.registrar(
                new MovimientoRequest("496825", TipoMovimiento.DEBITO, new BigDecimal("1"))))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void rechazaElRetiroQueSuperaElCupoDiario() {
        cuentaBloqueada("478758", "2000.00");
        when(movimientoRepository.sumarValores(eq(1L), eq(TipoMovimiento.DEBITO), any(), any(), isNull()))
                .thenReturn(new BigDecimal("-575.00"));

        assertThatThrownBy(() -> servicio.registrar(
                new MovimientoRequest("478758", TipoMovimiento.DEBITO, new BigDecimal("500"))))
                .isInstanceOf(CupoDiarioExcedidoException.class)
                .hasMessage("Cupo diario Excedido");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void rechazaMovimientosEnCuentasInactivas() {
        Cuenta cuenta = cuentaBloqueada("585545", "1000.00");
        cuenta.setEstado(false);

        assertThatThrownBy(() -> servicio.registrar(
                new MovimientoRequest("585545", TipoMovimiento.CREDITO, new BigDecimal("10"))))
                .isInstanceOfSatisfying(ReglaNegocioException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo(CodigoError.CUENTA_INACTIVA));
    }

    @Test
    void rechazaMovimientosDeClientesInactivos() {
        Cuenta cuenta = cuentaBloqueada("478758", "2000.00");
        cuenta.getCliente().setEstado(false);

        assertThatThrownBy(() -> servicio.registrar(
                new MovimientoRequest("478758", TipoMovimiento.CREDITO, new BigDecimal("10"))))
                .isInstanceOfSatisfying(ReglaNegocioException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo(CodigoError.CLIENTE_INACTIVO));
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void fallaSiLaCuentaNoExiste() {
        when(cuentaRepository.findByNumeroCuentaForUpdate("999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.registrar(
                new MovimientoRequest("999999", TipoMovimiento.CREDITO, BigDecimal.TEN)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe la cuenta 999999");
    }

    @Test
    void corrigeUnMovimientoYRecalculaLosSaldosDeLaCuenta() {
        Cuenta cuenta = Fixtures.cuenta(1L, "225487", "100.00", Fixtures.cliente(2L, "Marianela Montalvo"));
        Movimiento deposito = movimientoExistente(5L, cuenta, TipoMovimiento.CREDITO, "600.00", "700.00");

        MovimientoResponse respuesta = servicio.actualizar(5L,
                new MovimientoRequest("225487", TipoMovimiento.CREDITO, new BigDecimal("650")));

        assertThat(deposito.getValor()).isEqualByComparingTo("650.00");
        assertThat(respuesta.movimientoId()).isEqualTo(5L);
        verify(recalculadorSaldos).recalcular(cuenta);
    }

    @Test
    void noPermiteMoverUnMovimientoAOtraCuenta() {
        Cuenta cuenta = Fixtures.cuenta(1L, "225487", "100.00", Fixtures.cliente(2L, "Marianela Montalvo"));
        movimientoExistente(5L, cuenta, TipoMovimiento.CREDITO, "600.00", "700.00");

        assertThatThrownBy(() -> servicio.actualizar(5L,
                new MovimientoRequest("496825", TipoMovimiento.CREDITO, new BigDecimal("600"))))
                .isInstanceOfSatisfying(ReglaNegocioException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo(CodigoError.OPERACION_NO_PERMITIDA));
        verify(recalculadorSaldos, never()).recalcular(any());
    }

    @Test
    void laCorreccionParcialConservaLosCamposNoEnviados() {
        Cuenta cuenta = Fixtures.cuenta(1L, "478758", "2000.00", Fixtures.cliente(1L, "Jose Lema"));
        Movimiento retiro = movimientoExistente(6L, cuenta, TipoMovimiento.DEBITO, "-575.00", "1425.00");
        when(movimientoRepository.sumarValores(eq(1L), eq(TipoMovimiento.DEBITO), any(), any(), eq(6L)))
                .thenReturn(BigDecimal.ZERO);

        servicio.actualizarParcial(6L, new MovimientoPatchRequest(null, new BigDecimal("100")));

        assertThat(retiro.getTipoMovimiento()).isEqualTo(TipoMovimiento.DEBITO);
        assertThat(retiro.getValor()).isEqualByComparingTo("-100.00");
        verify(recalculadorSaldos).recalcular(cuenta);
    }

    @Test
    void laCorreccionValidaElSaldoPrevioAlMovimiento() {
        Cuenta cuenta = Fixtures.cuenta(1L, "496825", "540.00", Fixtures.cliente(2L, "Marianela Montalvo"));
        movimientoExistente(7L, cuenta, TipoMovimiento.DEBITO, "-540.00", "0.00");

        assertThatThrownBy(() -> servicio.actualizarParcial(7L, new MovimientoPatchRequest(null, new BigDecimal("541"))))
                .isInstanceOf(SaldoNoDisponibleException.class);
    }

    @Test
    void eliminaUnMovimientoYRecalculaLosSaldos() {
        Cuenta cuenta = Fixtures.cuenta(1L, "225487", "100.00", Fixtures.cliente(2L, "Marianela Montalvo"));
        Movimiento deposito = movimientoExistente(5L, cuenta, TipoMovimiento.CREDITO, "600.00", "700.00");

        servicio.eliminar(5L);

        verify(movimientoRepository).delete(deposito);
        verify(recalculadorSaldos).recalcular(cuenta);
    }

    @Test
    void obtenerFallaSiElMovimientoNoExiste() {
        when(movimientoRepository.findWithCuentaById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe un movimiento con id 99");
    }

    private Cuenta cuentaBloqueada(String numeroCuenta, String saldoInicial) {
        Cuenta cuenta = Fixtures.cuenta(1L, numeroCuenta, saldoInicial, Fixtures.cliente(1L, "Jose Lema"));
        when(cuentaRepository.findByNumeroCuentaForUpdate(numeroCuenta)).thenReturn(Optional.of(cuenta));
        return cuenta;
    }

    private Movimiento movimientoExistente(Long id, Cuenta cuenta, TipoMovimiento tipo, String valor, String saldo) {
        Movimiento movimiento = Fixtures.movimiento(id, cuenta, tipo, valor, saldo, AHORA.minusHours(1));
        cuenta.actualizarSaldoDisponible(new BigDecimal(saldo));
        when(movimientoRepository.findCuentaIdById(id)).thenReturn(Optional.of(cuenta.getId()));
        when(cuentaRepository.findByIdForUpdate(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findWithCuentaById(id)).thenReturn(Optional.of(movimiento));
        return movimiento;
    }

    private void guardarAsignandoId(Long id) {
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocacion -> {
            Movimiento movimiento = invocacion.getArgument(0);
            ReflectionTestUtils.setField(movimiento, "id", id);
            return movimiento;
        });
    }
}
