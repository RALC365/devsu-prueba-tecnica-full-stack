package com.devsu.banco.mapper;

import com.devsu.banco.dto.movimiento.MovimientoResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class MovimientoMapper {

    public MovimientoResponse toResponse(Movimiento movimiento) {
        Cuenta cuenta = movimiento.getCuenta();
        Cliente titular = cuenta.getCliente();
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getFecha(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo(),
                titular.getClienteId(),
                titular.getNombre());
    }
}
