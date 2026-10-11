package com.devsu.banco.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    static final String CODIGO = "codigo";
    static final String TIMESTAMP = "timestamp";
    static final String ERRORES = "errores";
    private static final String DATOS_INVALIDOS = "La solicitud contiene datos inválidos";

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(BancoException.class)
    public ResponseEntity<Object> handleBancoException(BancoException ex) {
        log.warn("Operación rechazada [{}]: {}", ex.getCodigo(), ex.getMessage());
        return responder(ex.getCodigo(), ex.getMessage(), null, HttpHeaders.EMPTY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errores = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violacion -> ultimoNodo(violacion.getPropertyPath()),
                        ConstraintViolation::getMessage,
                        GlobalExceptionHandler::unir,
                        LinkedHashMap::new));
        return responder(CodigoError.VALIDACION, DATOS_INVALIDOS, errores, HttpHeaders.EMPTY);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTransactionSystem(TransactionSystemException ex) {
        return ex.getRootCause() instanceof ConstraintViolationException violaciones
                ? handleConstraintViolation(violaciones)
                : handleInesperada(ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return responder(CodigoError.CONFLICTO_DE_DATOS,
                "El registro está duplicado o tiene información relacionada",
                null, HttpHeaders.EMPTY);
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<Object> handleConcurrencia(ConcurrencyFailureException ex) {
        log.warn("Conflicto de concurrencia: {}", ex.getMessage());
        return responder(CodigoError.CONFLICTO_DE_DATOS,
                "El registro está siendo modificado, intente de nuevo", null, HttpHeaders.EMPTY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInesperada(Exception ex) {
        log.error("Error no controlado", ex);
        return responder(CodigoError.ERROR_INTERNO,
                "Ocurrió un error inesperado", null, HttpHeaders.EMPTY);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.merge(error.getField(), mensaje(error.getDefaultMessage()), GlobalExceptionHandler::unir));
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errores.merge(error.getObjectName(), mensaje(error.getDefaultMessage()), GlobalExceptionHandler::unir));
        return responder(CodigoError.VALIDACION, DATOS_INVALIDOS, errores, headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return responder(CodigoError.SOLICITUD_INVALIDA, mensajeCuerpoIlegible(ex), null, headers);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return responder(CodigoError.SOLICITUD_INVALIDA,
                "El parámetro '%s' es obligatorio".formatted(ex.getParameterName()), null, headers);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return responder(CodigoError.SOLICITUD_INVALIDA,
                "El parámetro '%s' tiene un valor inválido: '%s'".formatted(ex.getPropertyName(), ex.getValue()),
                null, headers);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return responder(CodigoError.RECURSO_NO_ENCONTRADO,
                "No existe la ruta /" + ex.getResourcePath(), null, headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return responder(CodigoError.METODO_NO_PERMITIDO,
                "El método %s no está soportado para este recurso".formatted(ex.getMethod()), null, headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String tipo = ex.getContentType() == null
                ? "(no indicado)"
                : ex.getContentType().getType() + "/" + ex.getContentType().getSubtype();
        return responder(CodigoError.TIPO_CONTENIDO_NO_SOPORTADO,
                "Tipo de contenido no soportado: %s. Use application/json".formatted(tipo),
                null, headers);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> respuesta = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (respuesta != null && respuesta.getBody() instanceof ProblemDetail problema) {
            problema.setProperty(CODIGO, CodigoError.desdeEstado(statusCode.value()).name());
            problema.setProperty(TIMESTAMP, OffsetDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS));
        }
        return respuesta;
    }

    private ResponseEntity<Object> responder(CodigoError codigo, String detalle,
                                             @Nullable Map<String, String> errores, HttpHeaders headers) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(codigo.getStatus(), detalle);
        problema.setTitle(codigo.getTitulo());
        problema.setProperty(CODIGO, codigo.name());
        problema.setProperty(TIMESTAMP, OffsetDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS));
        if (errores != null && !errores.isEmpty()) {
            problema.setProperty(ERRORES, errores);
        }
        return ResponseEntity.status(codigo.getStatus()).headers(headers).body(problema);
    }

    private static String mensajeCuerpoIlegible(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof JacksonException jackson && !jackson.getPath().isEmpty()) {
            String campo = jackson.getPath().stream()
                    .map(referencia -> referencia.getPropertyName() != null
                            ? referencia.getPropertyName()
                            : "[" + referencia.getIndex() + "]")
                    .collect(Collectors.joining("."));
            Throwable causa = ex.getMostSpecificCause();
            return causa instanceof ValorInvalidoException
                    ? "Valor inválido en '%s': %s".formatted(campo, causa.getMessage())
                    : "Formato inválido en el campo '%s'".formatted(campo);
        }
        return "El cuerpo de la solicitud no es un JSON válido";
    }

    private static String ultimoNodo(Path path) {
        return StreamSupport.stream(path.spliterator(), false)
                .reduce((primero, segundo) -> segundo)
                .map(Path.Node::getName)
                .orElse(path.toString());
    }

    private static String mensaje(@Nullable String mensaje) {
        return Objects.requireNonNullElse(mensaje, "Valor inválido");
    }

    private static String unir(String actual, String nuevo) {
        return actual + "; " + nuevo;
    }
}
