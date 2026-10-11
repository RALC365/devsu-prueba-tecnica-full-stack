package com.devsu.banco.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CodigoError {
    VALIDACION(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos"),
    SOLICITUD_INVALIDA(HttpStatus.BAD_REQUEST, "Solicitud inválida"),
    RECURSO_NO_ENCONTRADO(HttpStatus.NOT_FOUND, "Recurso no encontrado"),
    METODO_NO_PERMITIDO(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido"),
    TIPO_CONTENIDO_NO_SOPORTADO(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado"),
    RECURSO_DUPLICADO(HttpStatus.CONFLICT, "Recurso duplicado"),
    CONFLICTO_DE_DATOS(HttpStatus.CONFLICT, "Conflicto con el estado actual de los datos"),
    SALDO_NO_DISPONIBLE(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio incumplida"),
    CUPO_DIARIO_EXCEDIDO(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio incumplida"),
    CUENTA_INACTIVA(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio incumplida"),
    CLIENTE_INACTIVO(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio incumplida"),
    OPERACION_NO_PERMITIDA(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio incumplida"),
    ERROR_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");

    private final HttpStatus status;
    private final String titulo;

    public static CodigoError desdeEstado(int status) {
        return switch (status) {
            case 404 -> RECURSO_NO_ENCONTRADO;
            case 405 -> METODO_NO_PERMITIDO;
            case 415 -> TIPO_CONTENIDO_NO_SOPORTADO;
            case 409 -> CONFLICTO_DE_DATOS;
            default -> status >= 500 ? ERROR_INTERNO : SOLICITUD_INVALIDA;
        };
    }
}
