package com.devsu.banco.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.banco.controller.ClienteController;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.service.ClienteService;
import com.devsu.banco.support.WebMvcTestConfig;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.TransactionSystemException;

@WebMvcTest(ClienteController.class)
@Import(WebMvcTestConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void unaViolacionDeIntegridadDevuelve409SinDetallesInternos() throws Exception {
        when(clienteService.obtener(1L)).thenThrow(
                new DataIntegrityViolationException("duplicate key value violates unique constraint uk_persona"));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_DE_DATOS"))
                .andExpect(jsonPath("$.detail").value(not(containsString("uk_persona"))));
    }

    @Test
    void unBloqueoConcurrenteDevuelve409() throws Exception {
        when(clienteService.obtener(1L)).thenThrow(new PessimisticLockingFailureException("lock timeout"));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("El registro está siendo modificado, intente de nuevo"));
    }

    @Test
    void unErrorInesperadoDevuelve500SinExponerLaCausa() throws Exception {
        when(clienteService.obtener(1L)).thenThrow(new IllegalStateException("NullPointer en capa interna"));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado"))
                .andExpect(content().string(not(containsString("NullPointer"))));
    }

    @Test
    void unaValidacionDelModeloDevuelve400ConElCampo() throws Exception {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Cliente invalido = new Cliente();
        invalido.setEstado(true);
        when(clienteService.obtener(1L)).thenThrow(new ConstraintViolationException(validator.validate(invalido)));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.identificacion").exists());
    }

    @Test
    void unaValidacionDelModeloAlConfirmarLaTransaccionTambienDevuelve400() throws Exception {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Cliente invalido = new Cliente();
        invalido.setEstado(true);
        when(clienteService.obtener(1L)).thenThrow(new TransactionSystemException("Could not commit JPA transaction",
                new ConstraintViolationException(validator.validate(invalido))));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    void otrosErroresDeTransaccionDevuelven500() throws Exception {
        when(clienteService.obtener(1L)).thenThrow(new TransactionSystemException("Could not commit JPA transaction"));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"));
    }

    @Test
    void unaRutaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.detail").value("No existe la ruta /api/no-existe"));
    }
}
