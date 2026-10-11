package com.devsu.banco.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.devsu.banco.dto.reporte.ReporteCuentaResponse;
import com.devsu.banco.dto.reporte.ReporteMovimientoResponse;
import com.devsu.banco.dto.reporte.ReportePdfResponse;
import com.devsu.banco.dto.reporte.ReporteResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.exception.SolicitudInvalidaException;
import com.devsu.banco.mapper.ReporteMapper;
import com.devsu.banco.report.ReportePdfGenerator;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.support.Fixtures;
import com.devsu.banco.support.RelojControlable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    private static final LocalDate INICIO = LocalDate.of(2022, 2, 1);
    private static final LocalDate FIN = LocalDate.of(2022, 2, 28);
    private static final LocalDateTime DESDE = INICIO.atStartOfDay();
    private static final LocalDateTime HASTA = FIN.plusDays(1).atStartOfDay();

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private MovimientoRepository movimientoRepository;
    @Mock
    private ReportePdfGenerator pdfGenerator;

    private ReporteServiceImpl servicio;
    private Cliente marianela;
    private Cuenta corriente;
    private Cuenta ahorros;

    @BeforeEach
    void setUp() {
        servicio = new ReporteServiceImpl(clienteRepository, cuentaRepository, movimientoRepository,
                new ReporteMapper(), pdfGenerator,
                new RelojControlable(LocalDateTime.of(2022, 3, 1, 8, 0), Fixtures.ZONA));
        marianela = Fixtures.cliente(2L, "Marianela Montalvo");
        corriente = Fixtures.cuenta(20L, "225487", "100.00", marianela);
        corriente.setTipoCuenta(TipoCuenta.CORRIENTE);
        corriente.actualizarSaldoDisponible(new BigDecimal("700.00"));
        ahorros = Fixtures.cuenta(21L, "496825", "540.00", marianela);
        ahorros.actualizarSaldoDisponible(new BigDecimal("0.00"));
    }

    @Test
    void generaElEstadoDeCuentaDelEjemploDelEnunciado() {
        Movimiento retiro = Fixtures.movimiento(1L, ahorros, TipoMovimiento.DEBITO, "-540.00", "0.00",
                LocalDateTime.of(2022, 2, 8, 10, 0));
        Movimiento deposito = Fixtures.movimiento(2L, corriente, TipoMovimiento.CREDITO, "600.00", "700.00",
                LocalDateTime.of(2022, 2, 10, 10, 0));
        prepararReporte(List.of(retiro, deposito), List.of(retiro, deposito));

        ReporteResponse reporte = servicio.generar(2L, INICIO, FIN);

        assertThat(reporte.cliente()).isEqualTo("Marianela Montalvo");
        assertThat(reporte.totalCreditos()).isEqualByComparingTo("600.00");
        assertThat(reporte.totalDebitos()).isEqualByComparingTo("-540.00");
        assertThat(reporte.fechaGeneracion()).isEqualTo(LocalDateTime.of(2022, 3, 1, 8, 0));
        assertThat(reporte.cuentas())
                .extracting(ReporteCuentaResponse::numeroCuenta, ReporteCuentaResponse::saldoDisponible,
                        ReporteCuentaResponse::totalCreditos, ReporteCuentaResponse::totalDebitos)
                .containsExactly(
                        tuple("225487", new BigDecimal("700.00"), new BigDecimal("600.00"), new BigDecimal("0.00")),
                        tuple("496825", new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("-540.00")));
        assertThat(reporte.movimientos())
                .extracting(ReporteMovimientoResponse::numeroCuenta, ReporteMovimientoResponse::saldoInicial,
                        ReporteMovimientoResponse::movimiento, ReporteMovimientoResponse::saldoDisponible)
                .containsExactly(
                        tuple("496825", new BigDecimal("540.00"), new BigDecimal("-540.00"), new BigDecimal("0.00")),
                        tuple("225487", new BigDecimal("100.00"), new BigDecimal("600.00"), new BigDecimal("700.00")));
    }

    @Test
    void cadaFilaParteDelSaldoPrevioAlMovimiento() {
        Movimiento primerRetiro = Fixtures.movimiento(1L, corriente, TipoMovimiento.DEBITO, "-40.00", "60.00",
                LocalDateTime.of(2022, 2, 10, 9, 0));
        Movimiento segundoRetiro = Fixtures.movimiento(2L, corriente, TipoMovimiento.DEBITO, "-60.00", "0.00",
                LocalDateTime.of(2022, 2, 10, 11, 0));
        prepararReporte(List.of(primerRetiro, segundoRetiro), List.of(segundoRetiro));

        ReporteResponse reporte = servicio.generar(2L, INICIO, FIN);

        assertThat(reporte.movimientos())
                .extracting(ReporteMovimientoResponse::saldoInicial, ReporteMovimientoResponse::saldoDisponible)
                .containsExactly(
                        tuple(new BigDecimal("100.00"), new BigDecimal("60.00")),
                        tuple(new BigDecimal("60.00"), new BigDecimal("0.00")));
    }

    @Test
    void elSaldoDeCadaCuentaEsElDelCierreDelPeriodo() {
        Movimiento ultimoDelPeriodo = Fixtures.movimiento(1L, corriente, TipoMovimiento.CREDITO, "50.00", "150.00",
                LocalDateTime.of(2022, 2, 20, 9, 0));
        prepararReporte(List.of(ultimoDelPeriodo), List.of(ultimoDelPeriodo));

        ReporteResponse reporte = servicio.generar(2L, INICIO, FIN);

        assertThat(reporte.cuentas())
                .extracting(ReporteCuentaResponse::numeroCuenta, ReporteCuentaResponse::saldoDisponible)
                .containsExactly(
                        tuple("225487", new BigDecimal("150.00")),
                        tuple("496825", new BigDecimal("540.00")));
    }

    @Test
    void sinMovimientosLasCuentasConservanSuSaldoInicial() {
        prepararReporte(List.of(), List.of());

        ReporteResponse reporte = servicio.generar(2L, INICIO, FIN);

        assertThat(reporte.movimientos()).isEmpty();
        assertThat(reporte.cuentas()).extracting(ReporteCuentaResponse::saldoDisponible)
                .containsExactly(new BigDecimal("100.00"), new BigDecimal("540.00"));
        assertThat(reporte.totalCreditos()).isEqualByComparingTo("0");
        assertThat(reporte.totalDebitos()).isEqualByComparingTo("0");
    }

    @Test
    void rechazaUnRangoDeFechasInvertido() {
        assertThatThrownBy(() -> servicio.generar(2L, FIN, INICIO))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessage("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    @Test
    void fallaSiElClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.generar(99L, INICIO, FIN))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void entregaElPdfEnBase64ConUnNombreDescriptivo() {
        prepararReporte(List.of(), List.of());
        byte[] pdf = "%PDF-1.7 contenido".getBytes(StandardCharsets.US_ASCII);
        when(pdfGenerator.generar(any(ReporteResponse.class))).thenReturn(pdf);

        ReportePdfResponse respuesta = servicio.generarPdf(2L, INICIO, FIN);

        assertThat(respuesta.nombreArchivo()).isEqualTo("estado-cuenta-2-20220201-20220228.pdf");
        assertThat(respuesta.tipoContenido()).isEqualTo("application/pdf");
        assertThat(Base64.getDecoder().decode(respuesta.contenidoBase64())).isEqualTo(pdf);
    }

    private void prepararReporte(List<Movimiento> delPeriodo, List<Movimiento> ultimosAntesDelCierre) {
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(marianela));
        when(movimientoRepository.findDelClienteEntre(2L, DESDE, HASTA)).thenReturn(delPeriodo);
        when(movimientoRepository.findUltimosDelClienteAntesDe(2L, HASTA)).thenReturn(ultimosAntesDelCierre);
        when(cuentaRepository.findByClienteIdOrderByNumeroCuentaAsc(2L)).thenReturn(List.of(corriente, ahorros));
    }
}
