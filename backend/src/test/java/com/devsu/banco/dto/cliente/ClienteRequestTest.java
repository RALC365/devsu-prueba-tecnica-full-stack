package com.devsu.banco.dto.cliente;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.banco.entity.Genero;
import org.junit.jupiter.api.Test;

class ClienteRequestTest {

    @Test
    void toStringNoExponeLaContrasena() {
        ClienteRequest request = new ClienteRequest("Jose Lema", Genero.MASCULINO, 35, "1712345678",
                "Otavalo sn y principal", "098254785", "secreta123", true);

        assertThat(request.toString()).contains("Jose Lema", "****").doesNotContain("secreta123");
    }

    @Test
    void toStringDelPatchNoExponeLaContrasena() {
        ClientePatchRequest conContrasena = new ClientePatchRequest(null, null, null, null, null, null, "secreta123", null);
        ClientePatchRequest sinContrasena = new ClientePatchRequest("Ana", null, null, null, null, null, null, null);

        assertThat(conContrasena.toString()).contains("****").doesNotContain("secreta123");
        assertThat(sinContrasena.toString()).contains("contrasena=null");
    }
}
