package com.devsu.banco.service;

import com.devsu.banco.dto.reporte.ReportePdfResponse;
import com.devsu.banco.dto.reporte.ReporteResponse;
import java.time.LocalDate;

public interface ReporteService {

    ReporteResponse generar(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);

    ReportePdfResponse generarPdf(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
