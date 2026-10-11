package com.devsu.banco.mapper;

import com.devsu.banco.dto.reporte.ReporteCuentaResponse;
import com.devsu.banco.dto.reporte.ReporteMovimientoResponse;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ReporteMapper {

    public ReporteCuentaResponse toResumenCuenta(Cuenta cuenta, BigDecimal saldoAlCierre,
                                                 BigDecimal totalCreditos, BigDecimal totalDebitos) {
        return new ReporteCuentaResponse(
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                saldoAlCierre,
                cuenta.getEstado(),
                totalCreditos,
                totalDebitos);
    }

    public ReporteMovimientoResponse toFila(Movimiento movimiento) {
        Cuenta cuenta = movimiento.getCuenta();
        return new ReporteMovimientoResponse(
                movimiento.getFecha(),
                cuenta.getCliente().getNombre(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                movimiento.getSaldoAnterior(),
                cuenta.getEstado(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo());
    }
}
