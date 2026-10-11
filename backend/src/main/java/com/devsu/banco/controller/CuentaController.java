package com.devsu.banco.controller;

import com.devsu.banco.dto.cuenta.CuentaPatchRequest;
import com.devsu.banco.dto.cuenta.CuentaRequest;
import com.devsu.banco.dto.cuenta.CuentaResponse;
import com.devsu.banco.service.CuentaService;
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
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
@Tag(name = "Cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping
    @Operation(summary = "Listar cuentas")
    public List<CuentaResponse> listar(@RequestParam(required = false) Long clienteId) {
        return cuentaService.listar(clienteId);
    }

    @GetMapping("/{cuentaId}")
    @Operation(summary = "Obtener cuenta")
    public CuentaResponse obtener(@PathVariable Long cuentaId) {
        return cuentaService.obtener(cuentaId);
    }

    @PostMapping
    @Operation(summary = "Crear cuenta")
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        CuentaResponse creada = cuentaService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creada.cuentaId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{cuentaId}")
    @Operation(summary = "Actualizar cuenta")
    public CuentaResponse actualizar(@PathVariable Long cuentaId, @Valid @RequestBody CuentaRequest request) {
        return cuentaService.actualizar(cuentaId, request);
    }

    @PatchMapping("/{cuentaId}")
    @Operation(summary = "Actualizar cuenta parcialmente")
    public CuentaResponse actualizarParcial(@PathVariable Long cuentaId,
                                            @Valid @RequestBody CuentaPatchRequest request) {
        return cuentaService.actualizarParcial(cuentaId, request);
    }

    @DeleteMapping("/{cuentaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar cuenta")
    public void eliminar(@PathVariable Long cuentaId) {
        cuentaService.eliminar(cuentaId);
    }
}
