package com.devsu.banco.controller;

import com.devsu.banco.dto.reporte.ReportePdfResponse;
import com.devsu.banco.dto.reporte.ReporteResponse;
import com.devsu.banco.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    @Operation(summary = "Estado de cuenta")
    public ReporteResponse generar(@RequestParam Long clienteId,
                                   @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate fechaInicio,
                                   @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate fechaFin) {
        return reporteService.generar(clienteId, fechaInicio, fechaFin);
    }

    @GetMapping("/pdf")
    @Operation(summary = "Estado de cuenta en PDF (base64)")
    public ReportePdfResponse generarPdf(@RequestParam Long clienteId,
                                         @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate fechaInicio,
                                         @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate fechaFin) {
        return reporteService.generarPdf(clienteId, fechaInicio, fechaFin);
    }
}
