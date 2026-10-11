package com.devsu.banco.support;

import com.devsu.banco.config.BancoProperties;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Genero;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public final class Fixtures {

    public static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    private Fixtures() {
    }

    public static BancoProperties propiedades(String limiteDiarioRetiro) {
        return new BancoProperties(ZONA,
                new BancoProperties.Movimientos(new BigDecimal(limiteDiarioRetiro)),
                new BancoProperties.Cors(List.of("http://localhost:4200")));
    }

    public static Cliente cliente(Long id, String nombre) {
        Cliente cliente = new Cliente();
        ReflectionTestUtils.setField(cliente, "id", id);
        cliente.setNombre(nombre);
        cliente.setGenero(Genero.FEMENINO);
        cliente.setEdad(30);
        cliente.setIdentificacion("17000000" + id);
        cliente.setDireccion("Amazonas y NNUU");
        cliente.setTelefono("097548965");
        cliente.setContrasena("$2a$10$hash");
        cliente.setEstado(true);
        return cliente;
    }

    public static Cuenta cuenta(Long id, String numeroCuenta, String saldoInicial, Cliente titular) {
        Cuenta cuenta = new Cuenta();
        ReflectionTestUtils.setField(cuenta, "id", id);
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setTipoCuenta(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(new BigDecimal(saldoInicial));
        cuenta.setEstado(true);
        cuenta.setCliente(titular);
        cuenta.inicializarSaldoDisponible();
        return cuenta;
    }

    public static Movimiento movimiento(Long id, Cuenta cuenta, TipoMovimiento tipo, String valor, String saldo,
                                        LocalDateTime fecha) {
        Movimiento movimiento = Movimiento.registrar(cuenta, tipo, new BigDecimal(valor), new BigDecimal(saldo), fecha);
        ReflectionTestUtils.setField(movimiento, "id", id);
        return movimiento;
    }
}
