package com.devsu.banco.report;

import com.devsu.banco.dto.reporte.ReporteCuentaResponse;
import com.devsu.banco.dto.reporte.ReporteMovimientoResponse;
import com.devsu.banco.dto.reporte.ReporteResponse;
import com.devsu.banco.entity.TipoCuenta;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

@Component
public class OpenPdfReporteGenerator implements ReportePdfGenerator {

    private static final Color AZUL = new Color(29, 79, 145);
    private static final Color GRIS_FONDO = new Color(243, 245, 248);
    private static final Color GRIS_BORDE = new Color(214, 219, 226);
    private static final Color VERDE = new Color(22, 128, 61);
    private static final Color ROJO = new Color(190, 18, 60);

    private static final Font MARCA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, AZUL);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, AZUL);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
    private static final Font TEXTO_ROJO = FontFactory.getFont(FontFactory.HELVETICA, 9, ROJO);
    private static final Font ENCABEZADO_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generar(ReporteResponse reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            agregarEncabezado(documento, reporte);
            agregarResumen(documento, reporte);
            agregarCuentas(documento, reporte);
            agregarMovimientos(documento, reporte);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del estado de cuenta", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return salida.toByteArray();
    }

    private void agregarEncabezado(Document documento, ReporteResponse reporte) {
        documento.add(new Paragraph("BANCO", MARCA));
        Paragraph titulo = new Paragraph("Estado de cuenta", SUBTITULO);
        titulo.setSpacingAfter(6);
        documento.add(titulo);
        documento.add(new Paragraph("Cliente: %s    Identificación: %s"
                .formatted(reporte.cliente(), reporte.identificacion()), TEXTO));
        documento.add(new Paragraph("Período: %s al %s"
                .formatted(reporte.fechaInicio().format(FECHA), reporte.fechaFin().format(FECHA)), TEXTO));
        Paragraph generado = new Paragraph("Generado el " + reporte.fechaGeneracion().format(FECHA_HORA), TEXTO);
        generado.setSpacingAfter(12);
        documento.add(generado);
    }

    private void agregarResumen(Document documento, ReporteResponse reporte) {
        PdfPTable resumen = new PdfPTable(4);
        resumen.setWidthPercentage(100);
        resumen.setSpacingAfter(14);
        resumen.addCell(celdaResumen("Total créditos", moneda(reporte.totalCreditos()), VERDE));
        resumen.addCell(celdaResumen("Total débitos", moneda(reporte.totalDebitos()), ROJO));
        resumen.addCell(celdaResumen("Cuentas", String.valueOf(reporte.cuentas().size()), AZUL));
        resumen.addCell(celdaResumen("Movimientos", String.valueOf(reporte.movimientos().size()), AZUL));
        documento.add(resumen);
    }

    private void agregarCuentas(Document documento, ReporteResponse reporte) {
        documento.add(seccion("Cuentas del cliente"));
        PdfPTable tabla = tabla(new float[]{2f, 1.4f, 1.2f, 1.6f, 1.6f, 1.6f, 1.8f},
                "Número cuenta", "Tipo", "Estado", "Saldo inicial", "Créditos", "Débitos", "Saldo al corte");
        for (ReporteCuentaResponse cuenta : reporte.cuentas()) {
            tabla.addCell(celda(cuenta.numeroCuenta()));
            tabla.addCell(celda(etiqueta(cuenta.tipoCuenta())));
            tabla.addCell(celda(estado(cuenta.estado())));
            tabla.addCell(celdaMonto(cuenta.saldoInicial()));
            tabla.addCell(celdaMonto(cuenta.totalCreditos()));
            tabla.addCell(celdaMonto(cuenta.totalDebitos()));
            tabla.addCell(celdaMonto(cuenta.saldoDisponible()));
        }
        documento.add(tabla);
    }

    private void agregarMovimientos(Document documento, ReporteResponse reporte) {
        documento.add(seccion("Movimientos del período"));
        if (reporte.movimientos().isEmpty()) {
            documento.add(new Paragraph("No se registraron movimientos en el período seleccionado.", TEXTO));
            return;
        }
        PdfPTable tabla = tabla(new float[]{1.7f, 2.4f, 1.6f, 1.3f, 1.5f, 1.1f, 1.5f, 1.6f},
                "Fecha", "Cliente", "Número cuenta", "Tipo", "Saldo inicial", "Estado", "Movimiento",
                "Saldo disponible");
        for (ReporteMovimientoResponse movimiento : reporte.movimientos()) {
            tabla.addCell(celda(movimiento.fecha().format(FECHA_HORA)));
            tabla.addCell(celda(movimiento.cliente()));
            tabla.addCell(celda(movimiento.numeroCuenta()));
            tabla.addCell(celda(etiqueta(movimiento.tipoCuenta())));
            tabla.addCell(celdaMonto(movimiento.saldoInicial()));
            tabla.addCell(celda(estado(movimiento.estado())));
            tabla.addCell(celdaMonto(movimiento.movimiento()));
            tabla.addCell(celdaMonto(movimiento.saldoDisponible()));
        }
        documento.add(tabla);
    }

    private static Paragraph seccion(String titulo) {
        Paragraph seccion = new Paragraph(titulo, SUBTITULO);
        seccion.setSpacingBefore(4);
        seccion.setSpacingAfter(6);
        return seccion;
    }

    private static PdfPTable tabla(float[] anchosRelativos, String... encabezados) {
        PdfPTable tabla = new PdfPTable(anchosRelativos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        tabla.setSpacingAfter(12);
        for (String encabezado : encabezados) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezado, ENCABEZADO_TABLA));
            celda.setBackgroundColor(AZUL);
            celda.setBorderColor(AZUL);
            celda.setPadding(6);
            tabla.addCell(celda);
        }
        return tabla;
    }

    private static PdfPCell celdaResumen(String etiqueta, String valor, Color color) {
        Phrase contenido = new Phrase();
        contenido.add(new Chunk(etiqueta + "\n", TEXTO));
        contenido.add(new Chunk(valor, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, color)));
        PdfPCell celda = new PdfPCell(contenido);
        celda.setPadding(8);
        celda.setBackgroundColor(GRIS_FONDO);
        celda.setBorderColor(GRIS_BORDE);
        return celda;
    }

    private static PdfPCell celda(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, TEXTO));
        celda.setPadding(5);
        celda.setBorderColor(GRIS_BORDE);
        return celda;
    }

    private static PdfPCell celdaMonto(BigDecimal monto) {
        PdfPCell celda = new PdfPCell(new Phrase(moneda(monto), monto.signum() < 0 ? TEXTO_ROJO : TEXTO));
        celda.setPadding(5);
        celda.setBorderColor(GRIS_BORDE);
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return celda;
    }

    static String moneda(BigDecimal valor) {
        String absoluto = String.format(Locale.US, "$%,.2f", valor.abs());
        return valor.signum() < 0 ? "-" + absoluto : absoluto;
    }

    private static String etiqueta(TipoCuenta tipo) {
        return switch (tipo) {
            case AHORROS -> "Ahorros";
            case CORRIENTE -> "Corriente";
        };
    }

    private static String estado(Boolean activa) {
        return Boolean.TRUE.equals(activa) ? "Activa" : "Inactiva";
    }
}
