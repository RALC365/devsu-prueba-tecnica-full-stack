package com.devsu.banco.dto.cliente;

final class ValidacionCliente {

    static final String IDENTIFICACION = "^[0-9A-Za-z-]{5,20}$";
    static final String IDENTIFICACION_MENSAJE =
            "La identificación debe tener entre 5 y 20 caracteres alfanuméricos";
    static final String TELEFONO = "^\\+?[0-9]{7,15}$";
    static final String TELEFONO_MENSAJE = "El teléfono debe contener entre 7 y 15 dígitos";
    static final String CONTRASENA_MENSAJE = "La contraseña debe tener entre 4 y 50 caracteres";
    static final String CONTRASENA_VACIA_MENSAJE = "La contraseña no puede estar vacía";
    static final int CONTRASENA_MAX_BYTES = 72; // límite de BCrypt
    static final String CONTRASENA_BYTES_MENSAJE = "La contraseña no puede superar los 72 bytes";

    private ValidacionCliente() {
    }
}
