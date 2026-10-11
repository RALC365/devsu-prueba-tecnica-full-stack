package com.devsu.banco.service.impl;

import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.exception.SaldoNoDisponibleException;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.util.Montos;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RecalculadorSaldos {

    private final MovimientoRepository movimientoRepository;

    void recalcular(Cuenta cuenta) {
        BigDecimal saldo = cuenta.getSaldoInicial();
        for (Movimiento movimiento : movimientoRepository.findByCuentaIdOrderByIdAsc(cuenta.getId())) {
            saldo = saldo.add(movimiento.getValor());
            if (saldo.signum() < 0) {
                throw new SaldoNoDisponibleException();
            }
            if (saldo.compareTo(Montos.MAXIMO) > 0) {
                throw new ReglaNegocioException(CodigoError.OPERACION_NO_PERMITIDA, Montos.MAXIMO_EXCEDIDO);
            }
            movimiento.setSaldo(saldo);
        }
        cuenta.actualizarSaldoDisponible(saldo);
    }
}
