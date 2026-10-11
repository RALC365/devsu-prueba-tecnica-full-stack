package com.devsu.banco.service.impl;

import com.devsu.banco.dto.reporte.ReporteCuentaResponse;
import com.devsu.banco.dto.reporte.ReportePdfResponse;
import com.devsu.banco.dto.reporte.ReporteResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.exception.SolicitudInvalidaException;
import com.devsu.banco.mapper.ReporteMapper;
import com.devsu.banco.report.ReportePdfGenerator;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.ReporteService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ReporteMapper reporteMapper;
    private final ReportePdfGenerator pdfGenerator;
    private final Clock clock;

    @Override
    public ReporteResponse generar(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new SolicitudInvalidaException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + clienteId));

        // hasta el final del día de fechaFin
        LocalDateTime desde = fechaInicio.atStartOfDay();
        LocalDateTime hasta = fechaFin.plusDays(1).atStartOfDay();
        List<Movimiento> movimientos = movimientoRepository.findDelClienteEntre(clienteId, desde, hasta);
        Map<Long, List<Movimiento>> movimientosPorCuenta = movimientos.stream()
                .collect(Collectors.groupingBy(movimiento -> movimiento.getCuenta().getId()));
        Map<Long, BigDecimal> saldosAlCierre = movimientoRepository.findUltimosDelClienteAntesDe(clienteId, hasta)
                .stream()
                .collect(Collectors.toMap(movimiento -> movimiento.getCuenta().getId(), Movimiento::getSaldo));

        List<ReporteCuentaResponse> cuentas = cuentaRepository.findByClienteIdOrderByNumeroCuentaAsc(clienteId)
                .stream()
                .map(cuenta -> {
                    TotalesMovimientos totales = TotalesMovimientos.de(
                            movimientosPorCuenta.getOrDefault(cuenta.getId(), List.of()));
                    BigDecimal saldoAlCierre = saldosAlCierre.getOrDefault(cuenta.getId(), cuenta.getSaldoInicial());
                    return reporteMapper.toResumenCuenta(cuenta, saldoAlCierre, totales.creditos(), totales.debitos());
                })
                .toList();
        TotalesMovimientos totales = TotalesMovimientos.de(movimientos);

        return new ReporteResponse(
                cliente.getClienteId(),
                cliente.getNombre(),
                cliente.getIdentificacion(),
                fechaInicio,
                fechaFin,
                LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS),
                totales.creditos(),
                totales.debitos(),
                cuentas,
                movimientos.stream().map(reporteMapper::toFila).toList());
    }

    @Override
    public ReportePdfResponse generarPdf(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        ReporteResponse reporte = generar(clienteId, fechaInicio, fechaFin);
        String nombreArchivo = "estado-cuenta-%d-%s-%s.pdf".formatted(clienteId,
                fechaInicio.format(DateTimeFormatter.BASIC_ISO_DATE),
                fechaFin.format(DateTimeFormatter.BASIC_ISO_DATE));
        String contenido = Base64.getEncoder().encodeToString(pdfGenerator.generar(reporte));
        return new ReportePdfResponse(nombreArchivo, MediaType.APPLICATION_PDF_VALUE, contenido);
    }
}
