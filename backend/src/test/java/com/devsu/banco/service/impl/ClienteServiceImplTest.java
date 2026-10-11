package com.devsu.banco.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devsu.banco.dto.cliente.ClientePatchRequest;
import com.devsu.banco.dto.cliente.ClienteRequest;
import com.devsu.banco.dto.cliente.ClienteResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Genero;
import com.devsu.banco.exception.ConflictoDatosException;
import com.devsu.banco.exception.RecursoDuplicadoException;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.mapper.ClienteMapper;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.support.Fixtures;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ClienteServiceImpl servicio;

    @BeforeEach
    void setUp() {
        servicio = new ClienteServiceImpl(clienteRepository, cuentaRepository, new ClienteMapper(), passwordEncoder);
    }

    @Test
    void creaElClienteGuardandoLaContrasenaCifrada() {
        when(clienteRepository.existsByIdentificacion("1712345678")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("$2a$10$cifrada");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> {
            Cliente cliente = invocacion.getArgument(0);
            ReflectionTestUtils.setField(cliente, "id", 1L);
            return cliente;
        });

        ClienteResponse respuesta = servicio.crear(joseLema("1234"));

        ArgumentCaptor<Cliente> guardado = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(guardado.capture());
        assertThat(guardado.getValue().getContrasena()).isEqualTo("$2a$10$cifrada");
        assertThat(respuesta.clienteId()).isEqualTo(1L);
        assertThat(respuesta.nombre()).isEqualTo("Jose Lema");
        assertThat(respuesta.estado()).isTrue();
    }

    @Test
    void rechazaUnaIdentificacionYaRegistrada() {
        when(clienteRepository.existsByIdentificacion("1712345678")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(joseLema("1234")))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya existe un cliente con la identificación 1712345678");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void actualizarSinContrasenaConservaLaActual() {
        Cliente existente = Fixtures.cliente(1L, "Jose");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByIdentificacionAndIdNot("1712345678", 1L)).thenReturn(false);

        ClienteResponse respuesta = servicio.actualizar(1L, joseLema(null));

        assertThat(respuesta.nombre()).isEqualTo("Jose Lema");
        assertThat(existente.getContrasena()).isEqualTo("$2a$10$hash");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void actualizarConContrasenaLaVuelveACifrar() {
        Cliente existente = Fixtures.cliente(1L, "Jose");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nueva")).thenReturn("$2a$10$nueva");

        servicio.actualizar(1L, joseLema("nueva"));

        assertThat(existente.getContrasena()).isEqualTo("$2a$10$nueva");
    }

    @Test
    void laActualizacionParcialSoloModificaLosCamposEnviados() {
        Cliente existente = Fixtures.cliente(2L, "Marianela Montalvo");
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(existente));

        ClienteResponse respuesta = servicio.actualizarParcial(2L,
                new ClientePatchRequest(null, null, null, null, "  Nueva dirección  ", null, null, false));

        assertThat(respuesta.direccion()).isEqualTo("Nueva dirección");
        assertThat(respuesta.estado()).isFalse();
        assertThat(respuesta.nombre()).isEqualTo("Marianela Montalvo");
        assertThat(respuesta.telefono()).isEqualTo("097548965");
    }

    @Test
    void noEliminaClientesConCuentasAsociadas() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(Fixtures.cliente(1L, "Jose Lema")));
        when(cuentaRepository.existsByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> servicio.eliminar(1L))
                .isInstanceOf(ConflictoDatosException.class)
                .hasMessage("No se puede eliminar el cliente porque tiene cuentas asociadas");
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void eliminaClientesSinCuentas() {
        Cliente cliente = Fixtures.cliente(3L, "Juan Osorio");
        when(clienteRepository.findById(3L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.existsByClienteId(3L)).thenReturn(false);

        servicio.eliminar(3L);

        verify(clienteRepository).delete(cliente);
    }

    @Test
    void obtenerFallaSiElClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe un cliente con id 99");
    }

    @Test
    void listaLosClientesSinExponerLaContrasena() {
        when(clienteRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(Fixtures.cliente(1L, "Jose Lema"), Fixtures.cliente(2L, "Marianela Montalvo")));

        assertThat(servicio.listar())
                .extracting(ClienteResponse::nombre)
                .containsExactly("Jose Lema", "Marianela Montalvo");
    }

    private static ClienteRequest joseLema(String contrasena) {
        return new ClienteRequest("Jose Lema", Genero.MASCULINO, 35, "1712345678",
                "Otavalo sn y principal", "098254785", contrasena, true);
    }
}
