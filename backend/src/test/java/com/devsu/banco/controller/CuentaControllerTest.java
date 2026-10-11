package com.devsu.banco.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.banco.dto.cuenta.CuentaPatchRequest;
import com.devsu.banco.dto.cuenta.CuentaRequest;
import com.devsu.banco.dto.cuenta.CuentaResponse;
import com.devsu.banco.entity.TipoCuenta;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.RecursoDuplicadoException;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.service.CuentaService;
import com.devsu.banco.support.WebMvcTestConfig;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CuentaController.class)
@Import(WebMvcTestConfig.class)
class CuentaControllerTest {

    private static final CuentaResponse CUENTA = new CuentaResponse(10L, "478758", TipoCuenta.AHORROS,
            new BigDecimal("2000.00"), new BigDecimal("2000.00"), true, 1L, "Jose Lema");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentaService cuentaService;

    @Test
    void crearAceptaElTipoDeCuentaComoApareceEnElEnunciado() throws Exception {
        when(cuentaService.crear(any(CuentaRequest.class))).thenReturn(CUENTA);
        String json = """
                {"numeroCuenta": "478758", "tipoCuenta": "Ahorro", "saldoInicial": 2000, "estado": true, "clienteId": 1}
                """;

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("478758"))
                .andExpect(jsonPath("$.saldoDisponible").value(2000.00))
                .andExpect(jsonPath("$.cliente").value("Jose Lema"));

        ArgumentCaptor<CuentaRequest> enviado = ArgumentCaptor.forClass(CuentaRequest.class);
        verify(cuentaService).crear(enviado.capture());
        assertThat(enviado.getValue().tipoCuenta()).isEqualTo(TipoCuenta.AHORROS);
    }

    @Test
    void crearValidaFormatoYMontos() throws Exception {
        String json = """
                {"numeroCuenta": "12AB", "tipoCuenta": "CORRIENTE", "saldoInicial": -5, "estado": true}
                """;

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numeroCuenta").value("El número de cuenta debe tener entre 6 y 20 dígitos"))
                .andExpect(jsonPath("$.errores.saldoInicial").value("El saldo inicial no puede ser negativo"))
                .andExpect(jsonPath("$.errores.clienteId").value("El cliente es obligatorio"));
        verifyNoInteractions(cuentaService);
    }

    @Test
    void unNumeroDeCuentaRepetidoDevuelve409() throws Exception {
        when(cuentaService.crear(any(CuentaRequest.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe una cuenta con el número 478758"));
        String json = """
                {"numeroCuenta": "478758", "tipoCuenta": "AHORROS", "saldoInicial": 10, "estado": true, "clienteId": 1}
                """;

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("RECURSO_DUPLICADO"));
    }

    @Test
    void unClienteInactivoDevuelve422() throws Exception {
        when(cuentaService.crear(any(CuentaRequest.class)))
                .thenThrow(new ReglaNegocioException(CodigoError.CLIENTE_INACTIVO, "El cliente Jose Lema está inactivo"));
        String json = """
                {"numeroCuenta": "585545", "tipoCuenta": "CORRIENTE", "saldoInicial": 1000, "estado": true, "clienteId": 1}
                """;

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_INACTIVO"))
                .andExpect(jsonPath("$.detail").value("El cliente Jose Lema está inactivo"));
    }

    @Test
    void listarPuedeFiltrarPorCliente() throws Exception {
        when(cuentaService.listar(1L)).thenReturn(List.of(CUENTA));

        mockMvc.perform(get("/api/cuentas").param("clienteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value("478758"));
    }

    @Test
    void obtenerDevuelveLaCuenta() throws Exception {
        when(cuentaService.obtener(10L)).thenReturn(CUENTA);

        mockMvc.perform(get("/api/cuentas/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaId").value(10))
                .andExpect(jsonPath("$.tipoCuenta").value("AHORROS"));
    }

    @Test
    void reemplazarDevuelveLaCuentaActualizada() throws Exception {
        when(cuentaService.actualizar(eq(10L), any(CuentaRequest.class))).thenReturn(CUENTA);
        String json = """
                {"numeroCuenta": "478758", "tipoCuenta": "AHORROS", "saldoInicial": 2000, "estado": true, "clienteId": 1}
                """;

        mockMvc.perform(put("/api/cuentas/10").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("478758"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/cuentas/10")).andExpect(status().isNoContent());

        verify(cuentaService).eliminar(10L);
    }

    @Test
    void actualizarParcialmenteSoloEnviaLosCamposIndicados() throws Exception {
        when(cuentaService.actualizarParcial(eq(10L), any(CuentaPatchRequest.class))).thenReturn(CUENTA);

        mockMvc.perform(patch("/api/cuentas/10").contentType(MediaType.APPLICATION_JSON).content("{\"estado\": false}"))
                .andExpect(status().isOk());

        verify(cuentaService).actualizarParcial(10L, new CuentaPatchRequest(null, null, null, false, null));
    }
}
