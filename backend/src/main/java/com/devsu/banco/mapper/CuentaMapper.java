package com.devsu.banco.mapper;

import com.devsu.banco.dto.cuenta.CuentaPatchRequest;
import com.devsu.banco.dto.cuenta.CuentaRequest;
import com.devsu.banco.dto.cuenta.CuentaResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.util.Montos;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaRequest request, Cliente titular) {
        Cuenta cuenta = new Cuenta();
        actualizar(cuenta, request);
        cuenta.setCliente(titular);
        return cuenta;
    }

    public void actualizar(Cuenta cuenta, CuentaRequest request) {
        cuenta.setNumeroCuenta(request.numeroCuenta());
        cuenta.setTipoCuenta(request.tipoCuenta());
        cuenta.setSaldoInicial(Montos.normalizar(request.saldoInicial()));
        cuenta.setEstado(request.estado());
    }

    public void actualizarParcial(Cuenta cuenta, CuentaPatchRequest request) {
        Optional.ofNullable(request.numeroCuenta()).ifPresent(cuenta::setNumeroCuenta);
        Optional.ofNullable(request.tipoCuenta()).ifPresent(cuenta::setTipoCuenta);
        Optional.ofNullable(request.saldoInicial()).map(Montos::normalizar).ifPresent(cuenta::setSaldoInicial);
        Optional.ofNullable(request.estado()).ifPresent(cuenta::setEstado);
    }

    public CuentaResponse toResponse(Cuenta cuenta) {
        Cliente titular = cuenta.getCliente();
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldoDisponible(),
                cuenta.getEstado(),
                titular.getClienteId(),
                titular.getNombre());
    }
}
