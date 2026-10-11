package com.devsu.banco.dto.cliente;

import com.devsu.banco.entity.Genero;

public record ClienteResponse(
        Long clienteId,
        String nombre,
        Genero genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        Boolean estado) {
}
