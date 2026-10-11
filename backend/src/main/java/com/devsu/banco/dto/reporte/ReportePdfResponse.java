package com.devsu.banco.dto.reporte;

public record ReportePdfResponse(
        String nombreArchivo,
        String tipoContenido,
        String contenidoBase64) {
}
