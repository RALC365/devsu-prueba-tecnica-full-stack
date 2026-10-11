package com.devsu.banco.dto.cliente;

import com.devsu.banco.entity.Genero;
import com.devsu.banco.validation.MaxUtf8Bytes;
import com.devsu.banco.validation.NullOrNotBlank;
import com.devsu.banco.validation.OnCreate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @Schema(example = "Jose Lema")
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Schema(example = "MASCULINO")
        @NotNull(message = "El género es obligatorio")
        Genero genero,

        @Schema(example = "35")
        @NotNull(message = "La edad es obligatoria")
        @Min(value = 1, message = "La edad debe ser mayor a 0")
        @Max(value = 120, message = "La edad no puede ser mayor a 120")
        Integer edad,

        @Schema(example = "1712345678")
        @NotBlank(message = "La identificación es obligatoria")
        @Pattern(regexp = ValidacionCliente.IDENTIFICACION, message = ValidacionCliente.IDENTIFICACION_MENSAJE)
        String identificacion,

        @Schema(example = "Otavalo sn y principal")
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion,

        @Schema(example = "098254785")
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = ValidacionCliente.TELEFONO, message = ValidacionCliente.TELEFONO_MENSAJE)
        String telefono,

        @Schema(example = "1234", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotNull(groups = OnCreate.class, message = "La contraseña es obligatoria")
        @NullOrNotBlank(message = ValidacionCliente.CONTRASENA_VACIA_MENSAJE)
        @Size(min = 4, max = 50, message = ValidacionCliente.CONTRASENA_MENSAJE)
        @MaxUtf8Bytes(value = ValidacionCliente.CONTRASENA_MAX_BYTES, message = ValidacionCliente.CONTRASENA_BYTES_MENSAJE)
        String contrasena,

        @Schema(example = "true")
        @NotNull(message = "El estado es obligatorio")
        Boolean estado) {

    @Override
    public String toString() {
        return "ClienteRequest[nombre=%s, identificacion=%s, estado=%s, contrasena=****]"
                .formatted(nombre, identificacion, estado);
    }
}
