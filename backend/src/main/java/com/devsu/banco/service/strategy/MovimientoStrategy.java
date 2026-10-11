package com.devsu.banco.service.strategy;

import com.devsu.banco.entity.TipoMovimiento;
import java.math.BigDecimal;

public interface MovimientoStrategy {

    TipoMovimiento getTipo();

    void validar(OperacionMovimiento operacion);

    BigDecimal aplicarSigno(BigDecimal monto);
}
