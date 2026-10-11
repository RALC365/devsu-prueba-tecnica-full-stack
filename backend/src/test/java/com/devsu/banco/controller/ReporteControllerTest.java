package com.devsu.banco.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.banco.dto.reporte.ReporteCuentaResponse;
import com.devsu.banco.dto.reporte.ReporteMovimientoResponse;
import com.devsu.banco.dto.reporte.ReportePdfResponse;
import com.devsu.banco.dto.reporte.ReporteResponse;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.SolicitudInvalidaException;
import com.devsu.banco.service.ReporteService;
import com.devsu.banco.support.WebMvcTestConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReporteController.class)
@Import(WebMvcTestConfig.class)
class ReporteControllerTest {

    private static final LocalDate INICIO = LocalDate.of(2022, 2, 1);
    private static final LocalDate FIN = LocalDate.of(2022, 2, 28);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @Test
    void devuelveElEstadoDeCuentaEnJson() throws Exception {
        when(reporteService.generar(2L, INICIO, FIN)).thenReturn(new ReporteResponse(2L, "Marianela Montalvo",
                "1700000002", INICIO, FIN, LocalDateTime.of(2022, 3, 1, 8, 0),
                new BigDecimal("600.00"), new BigDecimal("-540.00"),
                List.of(new ReporteCuentaResponse("225487", TipoCuenta.CORRIENTE, new BigDecimal("100.00"),
                        new BigDecimal("700.00"), true, new BigDecimal("600.00"), new BigDecimal("0.00"))),
                List.of(new ReporteMovimientoResponse(LocalDateTime.of(2022, 2, 10, 10, 0), "Marianela Montalvo",
                        "225487", TipoCuenta.CORRIENTE, new BigDecimal("100.00"), true, TipoMovimiento.CREDITO,
                        new BigDecimal("600.00"), new BigDecimal("700.00")))));

        mockMvc.perform(get("/api/reportes")
                        .param("clienteId", "2")
                        .param("fechaInicio", "2022-02-01")
                        .param("fechaFin", "2022-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaInicio").value("2022-02-01"))
                .andExpect(jsonPath("$.totalCreditos").value(600.00))
                .andExpect(jsonPath("$.totalDebitos").value(-540.00))
                .andExpect(jsonPath("$.cuentas[0].saldoDisponible").value(700.00))
                .andExpect(jsonPath("$.movimientos[0].cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.movimientos[0].movimiento").value(600.00))
                .andExpect(jsonPath("$.movimientos[0].saldoDisponible").value(700.00));
    }

    @Test
    void devuelveElPdfEnBase64() throws Exception {
        when(reporteService.generarPdf(2L, INICIO, FIN)).thenReturn(
                new ReportePdfResponse("estado-cuenta-2-20220201-20220228.pdf", "application/pdf", "JVBERi0xLjc="));

        mockMvc.perform(get("/api/reportes/pdf")
                        .param("clienteId", "2")
                        .param("fechaInicio", "2022-02-01")
                        .param("fechaFin", "2022-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreArchivo").value("estado-cuenta-2-20220201-20220228.pdf"))
                .andExpect(jsonPath("$.contenidoBase64").value("JVBERi0xLjc="));
    }

    @Test
    void exigeTodosLosParametros() throws Exception {
        mockMvc.perform(get("/api/reportes").param("clienteId", "2").param("fechaInicio", "2022-02-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El parámetro 'fechaFin' es obligatorio"));
        verifyNoInteractions(reporteService);
    }

    @Test
    void rechazaFechasConFormatoInvalido() throws Exception {
        mockMvc.perform(get("/api/reportes")
                        .param("clienteId", "2")
                        .param("fechaInicio", "01/02/2022")
                        .param("fechaFin", "2022-02-28"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("'fechaInicio'")));
    }

    @Test
    void propagaElErrorDeRangoInvalido() throws Exception {
        when(reporteService.generar(2L, FIN, INICIO)).thenThrow(
                new SolicitudInvalidaException("La fecha de inicio no puede ser posterior a la fecha de fin"));

        mockMvc.perform(get("/api/reportes")
                        .param("clienteId", "2")
                        .param("fechaInicio", "2022-02-28")
                        .param("fechaFin", "2022-02-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }
}
