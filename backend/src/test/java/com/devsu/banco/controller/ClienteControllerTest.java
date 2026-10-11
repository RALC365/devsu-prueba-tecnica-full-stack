package com.devsu.banco.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devsu.banco.dto.cliente.ClientePatchRequest;
import com.devsu.banco.dto.cliente.ClienteRequest;
import com.devsu.banco.dto.cliente.ClienteResponse;
import com.devsu.banco.entity.Genero;
import com.devsu.banco.exception.ConflictoDatosException;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.service.ClienteService;
import com.devsu.banco.support.WebMvcTestConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
@Import(WebMvcTestConfig.class)
class ClienteControllerTest {

    private static final String JOSE_LEMA = """
            {
              "nombre": "Jose Lema",
              "genero": "MASCULINO",
              "edad": 35,
              "identificacion": "1712345678",
              "direccion": "Otavalo sn y principal",
              "telefono": "098254785",
              "contrasena": "1234",
              "estado": true
            }
            """;

    private static final ClienteResponse JOSE_LEMA_CREADO = new ClienteResponse(1L, "Jose Lema", Genero.MASCULINO,
            35, "1712345678", "Otavalo sn y principal", "098254785", true);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void crearDevuelve201ConLaUbicacionDelRecursoYSinContrasena() throws Exception {
        when(clienteService.crear(any(ClienteRequest.class))).thenReturn(JOSE_LEMA_CREADO);

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(JOSE_LEMA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/clientes/1")))
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void crearSinContrasenaDevuelve400ConElDetallePorCampo() throws Exception {
        String sinContrasena = JOSE_LEMA.replace("\"contrasena\": \"1234\",", "");

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(sinContrasena))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.detail").value("La solicitud contiene datos inválidos"))
                .andExpect(jsonPath("$.errores.contrasena").value("La contraseña es obligatoria"));
        verifyNoInteractions(clienteService);
    }

    @Test
    void crearConVariosCamposInvalidosLosReportaTodos() throws Exception {
        String invalido = """
                {"nombre": " ", "genero": "FEMENINO", "edad": 0, "identificacion": "12",
                 "direccion": "Quito", "telefono": "abc", "contrasena": "12", "estado": true}
                """;

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.errores.edad").value("La edad debe ser mayor a 0"))
                .andExpect(jsonPath("$.errores.identificacion")
                        .value("La identificación debe tener entre 5 y 20 caracteres alfanuméricos"))
                .andExpect(jsonPath("$.errores.telefono").value("El teléfono debe contener entre 7 y 15 dígitos"))
                .andExpect(jsonPath("$.errores.contrasena").value("La contraseña debe tener entre 4 y 50 caracteres"));
    }

    @Test
    void unaContrasenaDeMasDe72BytesSeRechazaAntesDeCifrarla() throws Exception {
        // 25 caracteres, pero 75 bytes
        String contrasenaLarga = JOSE_LEMA.replace("\"1234\"", "\"" + "€".repeat(25) + "\"");

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(contrasenaLarga))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.contrasena").value("La contraseña no puede superar los 72 bytes"));
        verifyNoInteractions(clienteService);
    }

    @Test
    void unaContrasenaEnBlancoSeRechazaTambienAlActualizar() throws Exception {
        String contrasenaEnBlanco = JOSE_LEMA.replace("\"1234\"", "\"      \"");

        mockMvc.perform(put("/api/clientes/1").contentType(MediaType.APPLICATION_JSON).content(contrasenaEnBlanco))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.contrasena").value("La contraseña no puede estar vacía"));
        verifyNoInteractions(clienteService);
    }

    @Test
    void unGeneroDesconocidoDevuelveUnMensajeClaro() throws Exception {
        String generoInvalido = JOSE_LEMA.replace("\"MASCULINO\"", "\"X\"");

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(generoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"))
                .andExpect(jsonPath("$.detail").value(
                        "Valor inválido en 'genero': 'X' no es un valor válido. Valores permitidos: MASCULINO, FEMENINO, OTRO"));
    }

    @Test
    void unJsonMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_INVALIDA"));
    }

    @Test
    void actualizarConPutNoExigeLaContrasena() throws Exception {
        String sinContrasena = JOSE_LEMA.replace("\"contrasena\": \"1234\",", "");
        when(clienteService.actualizar(eq(1L), any(ClienteRequest.class))).thenReturn(JOSE_LEMA_CREADO);

        mockMvc.perform(put("/api/clientes/1").contentType(MediaType.APPLICATION_JSON).content(sinContrasena))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(1));
    }

    @Test
    void obtenerDevuelveElCliente() throws Exception {
        when(clienteService.obtener(1L)).thenReturn(JOSE_LEMA_CREADO);

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identificacion").value("1712345678"));
    }

    @Test
    void actualizarParcialmenteEnviaSoloLosCamposIndicados() throws Exception {
        when(clienteService.actualizarParcial(eq(1L), any(ClientePatchRequest.class))).thenReturn(JOSE_LEMA_CREADO);

        mockMvc.perform(patch("/api/clientes/1").contentType(MediaType.APPLICATION_JSON).content("{\"estado\": false}"))
                .andExpect(status().isOk());

        verify(clienteService).actualizarParcial(1L,
                new ClientePatchRequest(null, null, null, null, null, null, null, false));
    }

    @Test
    void actualizarParcialmenteRechazaUnNombreEnBlanco() throws Exception {
        mockMvc.perform(patch("/api/clientes/1").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre no puede estar vacío"));
    }

    @Test
    void obtenerUnClienteInexistenteDevuelve404ComoProblemDetail() throws Exception {
        when(clienteService.obtener(99L)).thenThrow(new RecursoNoEncontradoException("No existe un cliente con id 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("No existe un cliente con id 99"))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.instance").value("/api/clientes/99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/clientes/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("'clienteId'")));
    }

    @Test
    void listarDevuelveLosClientes() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(JOSE_LEMA_CREADO));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Jose Lema"))
                .andExpect(jsonPath("$[0].genero").value("MASCULINO"));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1")).andExpect(status().isNoContent());

        verify(clienteService).eliminar(1L);
    }

    @Test
    void eliminarUnClienteConCuentasDevuelve409() throws Exception {
        doThrow(new ConflictoDatosException("No se puede eliminar el cliente porque tiene cuentas asociadas"))
                .when(clienteService).eliminar(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO_DE_DATOS"))
                .andExpect(jsonPath("$.detail").value("No se puede eliminar el cliente porque tiene cuentas asociadas"));
    }
}
