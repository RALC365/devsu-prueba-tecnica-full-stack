package com.devsu.banco.service.strategy;

import com.devsu.banco.config.BancoProperties;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.CupoDiarioExcedidoException;
import com.devsu.banco.exception.SaldoNoDisponibleException;
import com.devsu.banco.repository.MovimientoRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DebitoStrategy implements MovimientoStrategy {

    private final MovimientoRepository movimientoRepository;
    private final BancoProperties properties;

    @Override
    public TipoMovimiento getTipo() {
        return TipoMovimiento.DEBITO;
    }

    @Override
    public void validar(OperacionMovimiento operacion) {
        if (operacion.saldoPrevio().compareTo(operacion.monto()) < 0) {
            throw new SaldoNoDisponibleException();
        }
        BigDecimal retirosDelDia = movimientoRepository.sumarValores(
                operacion.cuenta().getId(),
                TipoMovimiento.DEBITO,
                operacion.dia().atStartOfDay(),
                operacion.dia().plusDays(1).atStartOfDay(),
                operacion.movimientoExcluidoId()).abs();
        if (retirosDelDia.add(operacion.monto()).compareTo(properties.movimientos().limiteDiarioRetiro()) > 0) {
            throw new CupoDiarioExcedidoException();
        }
    }

    @Override
    public BigDecimal aplicarSigno(BigDecimal monto) {
        return monto.abs().negate();
    }
}
