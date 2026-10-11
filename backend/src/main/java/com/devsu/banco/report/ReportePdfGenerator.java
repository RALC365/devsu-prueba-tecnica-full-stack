package com.devsu.banco.report;

import com.devsu.banco.dto.reporte.ReporteResponse;

public interface ReportePdfGenerator {

    byte[] generar(ReporteResponse reporte);
}
