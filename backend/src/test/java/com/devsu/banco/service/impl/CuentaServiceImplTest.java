package com.devsu.banco.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devsu.banco.dto.cuenta.CuentaPatchRequest;
import com.devsu.banco.dto.cuenta.CuentaRequest;
import com.devsu.banco.dto.cuenta.CuentaResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.ConflictoDatosException;
import com.devsu.banco.exception.RecursoDuplicadoException;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.mapper.CuentaMapper;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.support.Fixtures;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private MovimientoRepository movimientoRepository;
    @Mock
    private RecalculadorSaldos recalculadorSaldos;

    private CuentaServiceImpl servicio;
    private Cliente jose;

    @BeforeEach
    void setUp() {
        servicio = new CuentaServiceImpl(cuentaRepository, clienteRepository, movimientoRepository,
                new CuentaMapper(), recalculadorSaldos);
        jose = Fixtures.cliente(1L, "Jose Lema");
    }

    @Test
    void creaLaCuentaConSaldoDisponibleIgualAlSaldoInicial() {
        when(cuentaRepository.existsByNumeroCuenta("478758")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(jose));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocacion -> {
            Cuenta cuenta = invocacion.getArgument(0);
            ReflectionTestUtils.setField(cuenta, "id", 10L);
            return cuenta;
        });

        CuentaResponse respuesta = servicio.crear(
                new CuentaRequest("478758", TipoCuenta.AHORROS, new BigDecimal("2000"), true, 1L));

        assertThat(respuesta.cuentaId()).isEqualTo(10L);
        assertThat(respuesta.saldoInicial()).isEqualByComparingTo("2000.00");
        assertThat(respuesta.saldoDisponible()).isEqualByComparingTo("2000.00");
        assertThat(respuesta.cliente()).isEqualTo("Jose Lema");
    }

    @Test
    void noCreaCuentasParaClientesInactivos() {
        jose.setEstado(false);
        when(cuentaRepository.existsByNumeroCuenta("585545")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(jose));

        assertThatThrownBy(() -> servicio.crear(
                new CuentaRequest("585545", TipoCuenta.CORRIENTE, new BigDecimal("1000"), true, 1L)))
                .isInstanceOfSatisfying(ReglaNegocioException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo(CodigoError.CLIENTE_INACTIVO));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void rechazaUnNumeroDeCuentaRepetido() {
        when(cuentaRepository.existsByNumeroCuenta("478758")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(
                new CuentaRequest("478758", TipoCuenta.AHORROS, BigDecimal.TEN, true, 1L)))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya existe una cuenta con el número 478758");
    }

    @Test
    void recalculaLosSaldosCuandoCambiaElSaldoInicial() {
        Cuenta cuenta = Fixtures.cuenta(10L, "225487", "100.00", jose);
        when(cuentaRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cuenta));

        servicio.actualizarParcial(10L, new CuentaPatchRequest(null, null, new BigDecimal("150"), null, null));

        assertThat(cuenta.getSaldoInicial()).isEqualByComparingTo("150.00");
        verify(recalculadorSaldos).recalcular(cuenta);
    }

    @Test
    void noRecalculaSiElSaldoInicialNoCambia() {
        Cuenta cuenta = Fixtures.cuenta(10L, "225487", "100.00", jose);
        when(cuentaRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.existsByNumeroCuentaAndIdNot("225487", 10L)).thenReturn(false);

        CuentaResponse respuesta = servicio.actualizar(10L,
                new CuentaRequest("225487", TipoCuenta.CORRIENTE, new BigDecimal("100.00"), false, 1L));

        assertThat(respuesta.tipoCuenta()).isEqualTo(TipoCuenta.CORRIENTE);
        assertThat(respuesta.estado()).isFalse();
        verify(recalculadorSaldos, never()).recalcular(any());
    }

    @Test
    void permiteCambiarElTitularPorOtroClienteActivo() {
        Cuenta cuenta = Fixtures.cuenta(10L, "225487", "100.00", jose);
        Cliente marianela = Fixtures.cliente(2L, "Marianela Montalvo");
        when(cuentaRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(marianela));

        CuentaResponse respuesta = servicio.actualizarParcial(10L,
                new CuentaPatchRequest(null, null, null, null, 2L));

        assertThat(respuesta.clienteId()).isEqualTo(2L);
        assertThat(respuesta.cliente()).isEqualTo("Marianela Montalvo");
    }

    @Test
    void noPermiteCambiarElTitularDeUnaCuentaConMovimientos() {
        Cuenta cuenta = Fixtures.cuenta(10L, "225487", "100.00", jose);
        when(cuentaRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.existsByCuentaId(10L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.actualizarParcial(10L, new CuentaPatchRequest(null, null, null, null, 2L)))
                .isInstanceOfSatisfying(ReglaNegocioException.class,
                        ex -> assertThat(ex.getCodigo()).isEqualTo(CodigoError.OPERACION_NO_PERMITIDA));
        assertThat(cuenta.getCliente()).isSameAs(jose);
    }

    @Test
    void noEliminaCuentasConMovimientos() {
        when(cuentaRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(Fixtures.cuenta(10L, "478758", "2000", jose)));
        when(movimientoRepository.existsByCuentaId(10L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.eliminar(10L))
                .isInstanceOf(ConflictoDatosException.class)
                .hasMessage("No se puede eliminar la cuenta porque tiene movimientos registrados");
        verify(cuentaRepository, never()).delete(any());
    }

    @Test
    void listarPuedeFiltrarPorCliente() {
        when(cuentaRepository.findByClienteIdOrderByNumeroCuentaAsc(1L))
                .thenReturn(List.of(Fixtures.cuenta(10L, "478758", "2000", jose)));

        assertThat(servicio.listar(1L)).extracting(CuentaResponse::numeroCuenta).containsExactly("478758");
        verify(cuentaRepository, never()).findAllByOrderByNumeroCuentaAsc();
    }
}
