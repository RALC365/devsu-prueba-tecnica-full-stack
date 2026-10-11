package com.devsu.banco.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.banco.dto.movimiento.MovimientoRequest;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.MovimientoService;
import com.devsu.banco.support.Fixtures;
import com.devsu.banco.support.RelojControlable;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CasosDeUsoIntegrationTest.RelojDePrueba.class)
class CasosDeUsoIntegrationTest {

    @TestConfiguration
    static class RelojDePrueba {

        @Bean
        @Primary
        RelojControlable relojDePrueba() {
            return new RelojControlable(LocalDateTime.of(2022, 2, 10, 9, 0), Fixtures.ZONA);
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RelojControlable reloj;
    @Autowired
    private MovimientoRepository movimientoRepository;
    @Autowired
    private CuentaRepository cuentaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private MovimientoService movimientoService;

    @AfterEach
    void limpiarBaseDeDatos() {
        movimientoRepository.deleteAllInBatch();
        cuentaRepository.deleteAllInBatch();
        clienteRepository.deleteAll();
        reloj.fijar(LocalDateTime.of(2022, 2, 10, 9, 0));
    }

    @Test
    void reproduceLosCasosDeUsoDelEnunciado() throws Exception {
        // 1. Clientes
        long jose = crearCliente("Jose Lema", "MASCULINO", "1710034065", "Otavalo sn y principal", "098254785", "1234");
        long marianela = crearCliente("Marianela Montalvo", "FEMENINO", "1710034066", "Amazonas y NNUU", "097548965", "5678");
        long juan = crearCliente("Juan Osorio", "MASCULINO", "1710034067", "13 junio y Equinoccial", "098874587", "1245");

        // 2. Cuentas
        crearCuenta("478758", "Ahorro", "2000", jose);
        crearCuenta("225487", "Corriente", "100", marianela);
        crearCuenta("495878", "Ahorros", "0", juan);
        crearCuenta("496825", "Ahorros", "540", marianela);

        // 3. Cuenta corriente para Jose Lema
        crearCuenta("585545", "Corriente", "1000", jose);

        // 4. Movimientos
        reloj.fijar(LocalDateTime.of(2022, 2, 8, 11, 0));
        registrar("496825", "Retiro", "540")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(-540.0))
                .andExpect(jsonPath("$.saldo").value(0.0));
        reloj.fijar(LocalDateTime.of(2022, 2, 10, 9, 0));
        registrar("478758", "Retiro", "575").andExpect(jsonPath("$.saldo").value(1425.0));
        registrar("225487", "Depósito", "600").andExpect(jsonPath("$.saldo").value(700.0));
        registrar("495878", "Depósito", "150").andExpect(jsonPath("$.saldo").value(150.0));

        registrar("496825", "DEBITO", "1")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Saldo no disponible"));
        registrar("478758", "DEBITO", "500")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Cupo diario Excedido"));
        registrar("478758", "DEBITO", "425")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(1000.0));

        // 5. Estado de cuenta
        estadoDeCuenta("/api/reportes", marianela)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.totalCreditos").value(600.0))
                .andExpect(jsonPath("$.totalDebitos").value(-540.0))
                .andExpect(jsonPath("$.cuentas.length()").value(2))
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("225487"))
                .andExpect(jsonPath("$.cuentas[0].saldoDisponible").value(700.0))
                .andExpect(jsonPath("$.cuentas[1].numeroCuenta").value("496825"))
                .andExpect(jsonPath("$.cuentas[1].saldoDisponible").value(0.0))
                .andExpect(jsonPath("$.movimientos.length()").value(2))
                .andExpect(jsonPath("$.movimientos[0].fecha").value("2022-02-08T11:00:00"))
                .andExpect(jsonPath("$.movimientos[0].numeroCuenta").value("496825"))
                .andExpect(jsonPath("$.movimientos[0].tipoCuenta").value("AHORROS"))
                .andExpect(jsonPath("$.movimientos[0].saldoInicial").value(540.0))
                .andExpect(jsonPath("$.movimientos[0].estado").value(true))
                .andExpect(jsonPath("$.movimientos[0].movimiento").value(-540.0))
                .andExpect(jsonPath("$.movimientos[0].saldoDisponible").value(0.0))
                .andExpect(jsonPath("$.movimientos[1].fecha").value("2022-02-10T09:00:00"))
                .andExpect(jsonPath("$.movimientos[1].cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.movimientos[1].numeroCuenta").value("225487"))
                .andExpect(jsonPath("$.movimientos[1].tipoCuenta").value("CORRIENTE"))
                .andExpect(jsonPath("$.movimientos[1].saldoInicial").value(100.0))
                .andExpect(jsonPath("$.movimientos[1].movimiento").value(600.0))
                .andExpect(jsonPath("$.movimientos[1].saldoDisponible").value(700.0));

        String pdf = contenido(estadoDeCuenta("/api/reportes/pdf", marianela).andExpect(status().isOk()));
        byte[] documento = Base64.getDecoder().decode(JsonPath.<String>read(pdf, "$.contenidoBase64"));
        assertThat(new String(documento, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");

        estadoDeCuenta("/api/reportes", jose)
                .andExpect(jsonPath("$.movimientos.length()").value(2))
                .andExpect(jsonPath("$.movimientos[0].saldoInicial").value(2000.0))
                .andExpect(jsonPath("$.movimientos[0].saldoDisponible").value(1425.0))
                .andExpect(jsonPath("$.movimientos[1].saldoInicial").value(1425.0))
                .andExpect(jsonPath("$.movimientos[1].saldoDisponible").value(1000.0))
                .andExpect(jsonPath("$.totalDebitos").value(-1000.0));

        mockMvc.perform(get("/api/reportes")
                        .param("clienteId", String.valueOf(marianela))
                        .param("fechaInicio", "2022-01-01")
                        .param("fechaFin", "2022-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movimientos.length()").value(0))
                .andExpect(jsonPath("$.cuentas[0].saldoDisponible").value(100.0))
                .andExpect(jsonPath("$.cuentas[1].saldoDisponible").value(540.0));
    }

    @Test
    void retirosSimultaneosNoPuedenUsarElMismoSaldo() throws Exception {
        long jose = crearCliente("Jose Lema", "MASCULINO", "1710034065", "Otavalo sn y principal", "098254785", "1234");
        long cuenta = crearCuenta("478758", "Ahorros", "1000", jose);
        int intentos = 5;
        ExecutorService ejecutor = Executors.newFixedThreadPool(intentos);
        CountDownLatch largada = new CountDownLatch(1);

        try {
            List<Future<Boolean>> resultados = IntStream.range(0, intentos)
                    .mapToObj(intento -> ejecutor.submit(() -> {
                        largada.await();
                        try {
                            movimientoService.registrar(
                                    new MovimientoRequest("478758", TipoMovimiento.DEBITO, new BigDecimal("300")));
                            return true;
                        } catch (ReglaNegocioException rechazo) {
                            return false;
                        }
                    }))
                    .toList();
            largada.countDown();

            long exitosos = 0;
            for (Future<Boolean> resultado : resultados) {
                if (resultado.get(30, TimeUnit.SECONDS)) {
                    exitosos++;
                }
            }

            assertThat(exitosos).isEqualTo(3);
            mockMvc.perform(get("/api/cuentas/{id}", cuenta)).andExpect(jsonPath("$.saldoDisponible").value(100.0));
            assertThat(movimientoRepository.findByCuentaIdOrderByIdAsc(cuenta))
                    .extracting(movimiento -> movimiento.getSaldo().intValue())
                    .containsExactly(700, 400, 100);
        } finally {
            ejecutor.shutdownNow();
        }
    }

    @Test
    void corregirOEliminarMovimientosRecalculaLosSaldosPosteriores() throws Exception {
        long marianela = crearCliente("Marianela Montalvo", "FEMENINO", "1710034066", "Amazonas y NNUU", "097548965", "5678");
        long cuenta = crearCuenta("225487", "Corriente", "100", marianela);
        long deposito = id(registrar("225487", "CREDITO", "600"), "$.movimientoId");
        long retiro = id(registrar("225487", "DEBITO", "650"), "$.movimientoId");

        mockMvc.perform(put("/api/movimientos/{id}", deposito).contentType(MediaType.APPLICATION_JSON)
                        .content(movimiento("225487", "CREDITO", "650")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(750.0));
        mockMvc.perform(get("/api/movimientos/{id}", retiro)).andExpect(jsonPath("$.saldo").value(100.0));

        mockMvc.perform(patch("/api/movimientos/{id}", deposito).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": 100}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Saldo no disponible"));
        mockMvc.perform(get("/api/cuentas/{id}", cuenta)).andExpect(jsonPath("$.saldoDisponible").value(100.0));

        mockMvc.perform(delete("/api/movimientos/{id}", retiro)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/cuentas/{id}", cuenta)).andExpect(jsonPath("$.saldoDisponible").value(750.0));

        mockMvc.perform(patch("/api/cuentas/{id}", cuenta).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"saldoInicial\": 200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoDisponible").value(850.0));
    }

    @Test
    void protegeLaIntegridadDeLaInformacion() throws Exception {
        long jose = crearCliente("Jose Lema", "MASCULINO", "1710034065", "Otavalo sn y principal", "098254785", "1234");
        long cuenta = crearCuenta("478758", "Ahorros", "2000", jose);
        registrar("478758", "DEBITO", "100").andExpect(status().isCreated());

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(cliente("Otro", "MASCULINO", "1710034065", "Quito", "0999999999", "9999")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"));
        mockMvc.perform(delete("/api/clientes/{id}", jose))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede eliminar el cliente porque tiene cuentas asociadas"));
        mockMvc.perform(delete("/api/cuentas/{id}", cuenta))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No se puede eliminar la cuenta porque tiene movimientos registrados"));

        mockMvc.perform(patch("/api/cuentas/{id}", cuenta).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": false}"))
                .andExpect(status().isOk());
        registrar("478758", "CREDITO", "10")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("CUENTA_INACTIVA"));

        mockMvc.perform(get("/api/clientes/{id}", jose)).andExpect(jsonPath("$.contrasena").doesNotExist());
        assertThat(clienteRepository.findById(jose).orElseThrow().getContrasena())
                .startsWith("$2a$")
                .isNotEqualTo("1234");
    }

    private long crearCliente(String nombre, String genero, String identificacion, String direccion,
                              String telefono, String contrasena) throws Exception {
        ResultActions resultado = mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(cliente(nombre, genero, identificacion, direccion, telefono, contrasena)))
                .andExpect(status().isCreated());
        return id(resultado, "$.clienteId");
    }

    private long crearCuenta(String numeroCuenta, String tipo, String saldoInicial, long clienteId) throws Exception {
        String json = """
                {"numeroCuenta": "%s", "tipoCuenta": "%s", "saldoInicial": %s, "estado": true, "clienteId": %d}
                """.formatted(numeroCuenta, tipo, saldoInicial, clienteId);
        ResultActions resultado = mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        return id(resultado, "$.cuentaId");
    }

    private ResultActions registrar(String numeroCuenta, String tipo, String valor) throws Exception {
        return mockMvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
                .content(movimiento(numeroCuenta, tipo, valor)));
    }

    private ResultActions estadoDeCuenta(String ruta, long clienteId) throws Exception {
        return mockMvc.perform(get(ruta)
                .param("clienteId", String.valueOf(clienteId))
                .param("fechaInicio", "2022-02-01")
                .param("fechaFin", "2022-02-28"));
    }

    private static String cliente(String nombre, String genero, String identificacion, String direccion,
                                  String telefono, String contrasena) {
        return """
                {"nombre": "%s", "genero": "%s", "edad": 30, "identificacion": "%s", "direccion": "%s",
                 "telefono": "%s", "contrasena": "%s", "estado": true}
                """.formatted(nombre, genero, identificacion, direccion, telefono, contrasena);
    }

    private static String movimiento(String numeroCuenta, String tipo, String valor) {
        return """
                {"numeroCuenta": "%s", "tipoMovimiento": "%s", "valor": %s}
                """.formatted(numeroCuenta, tipo, valor);
    }

    private static long id(ResultActions resultado, String ruta) throws Exception {
        Number id = JsonPath.read(contenido(resultado), ruta);
        return id.longValue();
    }

    private static String contenido(ResultActions resultado) throws Exception {
        return resultado.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
