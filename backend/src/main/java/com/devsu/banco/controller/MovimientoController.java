package com.devsu.banco.controller;

import com.devsu.banco.dto.movimiento.MovimientoPatchRequest;
import com.devsu.banco.dto.movimiento.MovimientoRequest;
import com.devsu.banco.dto.movimiento.MovimientoResponse;
import com.devsu.banco.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    @GetMapping
    @Operation(summary = "Listar movimientos")
    public List<MovimientoResponse> listar(@RequestParam(required = false) String numeroCuenta) {
        return movimientoService.listar(numeroCuenta);
    }

    @GetMapping("/{movimientoId}")
    @Operation(summary = "Obtener movimiento")
    public MovimientoResponse obtener(@PathVariable Long movimientoId) {
        return movimientoService.obtener(movimientoId);
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento")
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        MovimientoResponse registrado = movimientoService.registrar(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(registrado.movimientoId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(registrado);
    }

    @PutMapping("/{movimientoId}")
    @Operation(summary = "Actualizar movimiento")
    public MovimientoResponse actualizar(@PathVariable Long movimientoId,
                                         @Valid @RequestBody MovimientoRequest request) {
        return movimientoService.actualizar(movimientoId, request);
    }

    @PatchMapping("/{movimientoId}")
    @Operation(summary = "Actualizar movimiento parcialmente")
    public MovimientoResponse actualizarParcial(@PathVariable Long movimientoId,
                                                @Valid @RequestBody MovimientoPatchRequest request) {
        return movimientoService.actualizarParcial(movimientoId, request);
    }

    @DeleteMapping("/{movimientoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar movimiento")
    public void eliminar(@PathVariable Long movimientoId) {
        movimientoService.eliminar(movimientoId);
    }
}
