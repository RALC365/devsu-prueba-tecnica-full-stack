package com.devsu.banco.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.banco.dto.movimiento.MovimientoPatchRequest;
import com.devsu.banco.dto.movimiento.MovimientoRequest;
import com.devsu.banco.dto.movimiento.MovimientoResponse;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.CupoDiarioExcedidoException;
import com.devsu.banco.exception.SaldoNoDisponibleException;
import com.devsu.banco.service.MovimientoService;
import com.devsu.banco.support.WebMvcTestConfig;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MovimientoController.class)
@Import(WebMvcTestConfig.class)
class MovimientoControllerTest {

    private static final String RETIRO = """
            {"numeroCuenta": "478758", "tipoMovimiento": "DEBITO", "valor": 575}
            """;

    private static final MovimientoResponse RETIRO_REGISTRADO = new MovimientoResponse(1L,
            LocalDateTime.of(2022, 2, 10, 10, 15, 30), "478758", TipoCuenta.AHORROS, TipoMovimiento.DEBITO,
            new BigDecimal("-575.00"), new BigDecimal("1425.00"), 1L, "Jose Lema");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimientoService movimientoService;

    @Test
    void registrarDevuelve201ConElValorConSignoYElSaldo() throws Exception {
        when(movimientoService.registrar(any(MovimientoRequest.class))).thenReturn(RETIRO_REGISTRADO);

        mockMvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/movimientos/1")))
                .andExpect(jsonPath("$.valor").value(-575.00))
                .andExpect(jsonPath("$.saldo").value(1425.00))
                .andExpect(jsonPath("$.fecha").value("2022-02-10T10:15:30"));
    }

    @Test
    void unRetiroSinSaldoDevuelve422ConElMensajeDelEnunciado() throws Exception {
        when(movimientoService.registrar(any(MovimientoRequest.class))).thenThrow(new SaldoNoDisponibleException());

        mockMvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Saldo no disponible"))
                .andExpect(jsonPath("$.codigo").value("SALDO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.title").value("Regla de negocio incumplida"));
    }

    @Test
    void unRetiroSobreElCupoDiarioDevuelve422ConElMensajeDelEnunciado() throws Exception {
        when(movimientoService.registrar(any(MovimientoRequest.class))).thenThrow(new CupoDiarioExcedidoException());

        mockMvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Cupo diario Excedido"))
                .andExpect(jsonPath("$.codigo").value("CUPO_DIARIO_EXCEDIDO"));
    }

    @Test
    void elValorDebeSerPositivo() throws Exception {
        String valorNegativo = """
                {"numeroCuenta": "478758", "tipoMovimiento": "DEBITO", "valor": -575}
                """;

        mockMvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON).content(valorNegativo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.valor").value("El valor debe ser mayor a 0"));
    }

    @Test
    void aceptaRetiroYDepositoComoAliasDelTipo() throws Exception {
        when(movimientoService.registrar(any(MovimientoRequest.class))).thenReturn(RETIRO_REGISTRADO);
        String conAlias = """
                {"numeroCuenta": "478758", "tipoMovimiento": "Retiro", "valor": 575}
                """;

        mockMvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON).content(conAlias))
                .andExpect(status().isCreated());

        ArgumentCaptor<MovimientoRequest> enviado = ArgumentCaptor.forClass(MovimientoRequest.class);
        verify(movimientoService).registrar(enviado.capture());
        assertThat(enviado.getValue().tipoMovimiento()).isEqualTo(TipoMovimiento.DEBITO);
    }

    @Test
    void listarPuedeFiltrarPorNumeroDeCuenta() throws Exception {
        when(movimientoService.listar("478758")).thenReturn(List.of(RETIRO_REGISTRADO));

        mockMvc.perform(get("/api/movimientos").param("numeroCuenta", "478758"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoMovimiento").value("DEBITO"));
    }

    @Test
    void corregirDevuelveElMovimientoActualizado() throws Exception {
        when(movimientoService.actualizar(eq(1L), any(MovimientoRequest.class))).thenReturn(RETIRO_REGISTRADO);

        mockMvc.perform(put("/api/movimientos/1").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movimientoId").value(1));
    }

    @Test
    void obtenerDevuelveElMovimiento() throws Exception {
        when(movimientoService.obtener(1L)).thenReturn(RETIRO_REGISTRADO);

        mockMvc.perform(get("/api/movimientos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Jose Lema"));
    }

    @Test
    void corregirParcialmenteEnviaSoloLosCamposIndicados() throws Exception {
        when(movimientoService.actualizarParcial(eq(1L), any(MovimientoPatchRequest.class)))
                .thenReturn(RETIRO_REGISTRADO);

        mockMvc.perform(patch("/api/movimientos/1").contentType(MediaType.APPLICATION_JSON).content("{\"valor\": 100}"))
                .andExpect(status().isOk());

        verify(movimientoService).actualizarParcial(1L, new MovimientoPatchRequest(null, new BigDecimal("100")));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/movimientos/1")).andExpect(status().isNoContent());

        verify(movimientoService).eliminar(1L);
    }

    @Test
    void unMetodoNoSoportadoDevuelve405() throws Exception {
        mockMvc.perform(post("/api/movimientos/1").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("METODO_NO_PERMITIDO"))
                .andExpect(jsonPath("$.detail").value("El método POST no está soportado para este recurso"));
    }

    @Test
    void unTipoDeContenidoNoSoportadoDevuelve415() throws Exception {
        mockMvc.perform(post("/api/movimientos").contentType(MediaType.TEXT_PLAIN).content(RETIRO))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.codigo").value("TIPO_CONTENIDO_NO_SOPORTADO"))
                .andExpect(jsonPath("$.detail")
                        .value("Tipo de contenido no soportado: text/plain. Use application/json"));
    }
}
