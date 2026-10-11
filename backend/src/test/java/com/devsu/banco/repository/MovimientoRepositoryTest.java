package com.devsu.banco.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Genero;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MovimientoRepositoryTest {

    private static final LocalDate DIA = LocalDate.of(2022, 2, 10);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MovimientoRepository movimientoRepository;

    private Cliente cliente;
    private Cuenta cuenta;
    private Movimiento retiroDeLaManana;

    @BeforeEach
    void setUp() {
        cliente = entityManager.persist(cliente("Jose Lema", "1712345678"));
        cuenta = entityManager.persist(cuenta("478758", cliente));
        retiroDeLaManana = registrar(TipoMovimiento.DEBITO, "-300.00", DIA.atTime(9, 0));
        registrar(TipoMovimiento.DEBITO, "-200.00", DIA.atTime(18, 30));
        registrar(TipoMovimiento.CREDITO, "500.00", DIA.atTime(10, 0));
        registrar(TipoMovimiento.DEBITO, "-100.00", DIA.minusDays(1).atTime(23, 59));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void sumaSoloLosDebitosDelDia() {
        BigDecimal total = movimientoRepository.sumarValores(cuenta.getId(), TipoMovimiento.DEBITO,
                DIA.atStartOfDay(), DIA.plusDays(1).atStartOfDay(), null);

        assertThat(total).isEqualByComparingTo("-500.00");
    }

    @Test
    void excluyeDelTotalElMovimientoIndicado() {
        BigDecimal total = movimientoRepository.sumarValores(cuenta.getId(), TipoMovimiento.DEBITO,
                DIA.atStartOfDay(), DIA.plusDays(1).atStartOfDay(), retiroDeLaManana.getId());

        assertThat(total).isEqualByComparingTo("-200.00");
    }

    @Test
    void devuelveCeroSiNoHayMovimientosEnElIntervalo() {
        BigDecimal total = movimientoRepository.sumarValores(cuenta.getId(), TipoMovimiento.DEBITO,
                DIA.plusDays(5).atStartOfDay(), DIA.plusDays(6).atStartOfDay(), null);

        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    void obtieneLosMovimientosDelClienteEnElRangoConSuCuentaYTitularCargados() {
        List<Movimiento> movimientos = movimientoRepository.findDelClienteEntre(cliente.getId(),
                DIA.atStartOfDay(), DIA.plusDays(1).atStartOfDay());

        assertThat(movimientos).extracting(Movimiento::getValor)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("-300"), new BigDecimal("500"), new BigDecimal("-200"));
        assertThat(Hibernate.isInitialized(movimientos.getFirst().getCuenta())).isTrue();
        assertThat(Hibernate.isInitialized(movimientos.getFirst().getCuenta().getCliente())).isTrue();
    }

    @Test
    void obtieneElHistorialDeLaCuentaEnElOrdenEnQueSeRegistro() {
        assertThat(movimientoRepository.findByCuentaIdOrderByIdAsc(cuenta.getId()))
                .extracting(Movimiento::getId)
                .isSorted()
                .hasSize(4);
    }

    @Test
    void obtieneElUltimoMovimientoDeCadaCuentaAntesDelCierre() {
        assertThat(movimientoRepository.findUltimosDelClienteAntesDe(cliente.getId(), DIA.atStartOfDay()))
                .singleElement()
                .satisfies(ultimo -> assertThat(ultimo.getSaldo()).isEqualByComparingTo("1900.00"));
        assertThat(movimientoRepository.findUltimosDelClienteAntesDe(cliente.getId(), DIA.minusDays(10).atStartOfDay()))
                .isEmpty();
    }

    @Test
    void obtieneLaCuentaDeUnMovimientoSinCargarlo() {
        assertThat(movimientoRepository.findCuentaIdById(retiroDeLaManana.getId())).contains(cuenta.getId());
        assertThat(movimientoRepository.existsByCuentaId(cuenta.getId())).isTrue();
    }

    private Movimiento registrar(TipoMovimiento tipo, String valor, LocalDateTime fecha) {
        BigDecimal monto = new BigDecimal(valor);
        BigDecimal saldo = cuenta.aplicarMovimiento(monto);
        return entityManager.persist(Movimiento.registrar(cuenta, tipo, monto, saldo, fecha));
    }

    private static Cliente cliente(String nombre, String identificacion) {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setGenero(Genero.MASCULINO);
        cliente.setEdad(35);
        cliente.setIdentificacion(identificacion);
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("098254785");
        cliente.setContrasena("$2a$10$hash");
        cliente.setEstado(true);
        return cliente;
    }

    private static Cuenta cuenta(String numero, Cliente titular) {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(numero);
        cuenta.setTipoCuenta(TipoCuenta.AHORROS);
        cuenta.setSaldoInicial(new BigDecimal("2000.00"));
        cuenta.setEstado(true);
        cuenta.setCliente(titular);
        cuenta.inicializarSaldoDisponible();
        return cuenta;
    }
}
