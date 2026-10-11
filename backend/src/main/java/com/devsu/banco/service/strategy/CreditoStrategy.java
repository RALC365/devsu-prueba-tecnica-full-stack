package com.devsu.banco.service.strategy;

import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.util.Montos;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class CreditoStrategy implements MovimientoStrategy {

    @Override
    public TipoMovimiento getTipo() {
        return TipoMovimiento.CREDITO;
    }

    @Override
    public void validar(OperacionMovimiento operacion) {
        if (operacion.saldoPrevio().add(operacion.monto()).compareTo(Montos.MAXIMO) > 0) {
            throw new ReglaNegocioException(CodigoError.OPERACION_NO_PERMITIDA, Montos.MAXIMO_EXCEDIDO);
        }
    }

    @Override
    public BigDecimal aplicarSigno(BigDecimal monto) {
        return monto.abs();
    }
}
