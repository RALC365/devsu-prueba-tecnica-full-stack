package com.devsu.banco.report;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.banco.dto.reporte.ReporteCuentaResponse;
import com.devsu.banco.dto.reporte.ReporteMovimientoResponse;
import com.devsu.banco.dto.reporte.ReporteResponse;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

class OpenPdfReporteGeneratorTest {

    private final OpenPdfReporteGenerator generador = new OpenPdfReporteGenerator();

    @Test
    void generaUnPdfConLosDatosDelEstadoDeCuenta() throws IOException {
        byte[] pdf = generador.generar(reporte(List.of(
                new ReporteMovimientoResponse(LocalDateTime.of(2022, 2, 10, 10, 0), "Marianela Montalvo", "225487",
                        TipoCuenta.CORRIENTE, monto("100.00"), true, TipoMovimiento.CREDITO, monto("600.00"),
                        monto("700.00")))));

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        String texto = textoDe(pdf);
        assertThat(texto).contains("Estado de cuenta", "Marianela Montalvo", "225487", "10/02/2022", "$600.00", "$700.00");
    }

    @Test
    void generaElPdfAunqueNoHayaMovimientosEnElPeriodo() throws IOException {
        byte[] pdf = generador.generar(reporte(List.of()));

        assertThat(textoDe(pdf)).contains("No se registraron movimientos en el período seleccionado.");
    }

    @Test
    void formateaLosMontosComoMonedaConSigno() {
        assertThat(OpenPdfReporteGenerator.moneda(new BigDecimal("1425.5"))).isEqualTo("$1,425.50");
        assertThat(OpenPdfReporteGenerator.moneda(new BigDecimal("-540"))).isEqualTo("-$540.00");
    }

    private static ReporteResponse reporte(List<ReporteMovimientoResponse> movimientos) {
        return new ReporteResponse(2L, "Marianela Montalvo", "1700000002",
                LocalDate.of(2022, 2, 1), LocalDate.of(2022, 2, 28), LocalDateTime.of(2022, 3, 1, 8, 0),
                monto("600.00"), monto("0.00"),
                List.of(new ReporteCuentaResponse("225487", TipoCuenta.CORRIENTE, monto("100.00"), monto("700.00"),
                        true, monto("600.00"), monto("0.00"))),
                movimientos);
    }

    private static String textoDe(byte[] pdf) throws IOException {
        try (PdfReader lector = new PdfReader(pdf)) {
            return new PdfTextExtractor(lector).getTextFromPage(1);
        }
    }

    private static BigDecimal monto(String valor) {
        return new BigDecimal(valor);
    }
}
