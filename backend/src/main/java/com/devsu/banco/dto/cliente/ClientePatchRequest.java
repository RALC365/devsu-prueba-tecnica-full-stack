package com.devsu.banco.dto.cliente;

import com.devsu.banco.entity.Genero;
import com.devsu.banco.validation.MaxUtf8Bytes;
import com.devsu.banco.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientePatchRequest(
        @NullOrNotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        Genero genero,

        @Min(value = 1, message = "La edad debe ser mayor a 0")
        @Max(value = 120, message = "La edad no puede ser mayor a 120")
        Integer edad,

        @Pattern(regexp = ValidacionCliente.IDENTIFICACION, message = ValidacionCliente.IDENTIFICACION_MENSAJE)
        String identificacion,

        @NullOrNotBlank(message = "La dirección no puede estar vacía")
        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion,

        @Pattern(regexp = ValidacionCliente.TELEFONO, message = ValidacionCliente.TELEFONO_MENSAJE)
        String telefono,

        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        @NullOrNotBlank(message = ValidacionCliente.CONTRASENA_VACIA_MENSAJE)
        @Size(min = 4, max = 50, message = ValidacionCliente.CONTRASENA_MENSAJE)
        @MaxUtf8Bytes(value = ValidacionCliente.CONTRASENA_MAX_BYTES, message = ValidacionCliente.CONTRASENA_BYTES_MENSAJE)
        String contrasena,

        Boolean estado) {

    @Override
    public String toString() {
        return "ClientePatchRequest[nombre=%s, identificacion=%s, estado=%s, contrasena=%s]"
                .formatted(nombre, identificacion, estado, contrasena == null ? null : "****");
    }
}
