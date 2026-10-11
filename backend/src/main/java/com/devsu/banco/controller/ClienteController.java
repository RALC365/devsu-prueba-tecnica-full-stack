package com.devsu.banco.controller;

import com.devsu.banco.dto.cliente.ClientePatchRequest;
import com.devsu.banco.dto.cliente.ClienteRequest;
import com.devsu.banco.dto.cliente.ClienteResponse;
import com.devsu.banco.service.ClienteService;
import com.devsu.banco.validation.OnCreate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listar clientes")
    public List<ClienteResponse> listar() {
        return clienteService.listar();
    }

    @GetMapping("/{clienteId}")
    @Operation(summary = "Obtener cliente")
    public ClienteResponse obtener(@PathVariable Long clienteId) {
        return clienteService.obtener(clienteId);
    }

    @PostMapping
    @Operation(summary = "Crear cliente")
    public ResponseEntity<ClienteResponse> crear(@Validated(OnCreate.class) @RequestBody ClienteRequest request) {
        ClienteResponse creado = clienteService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(creado.clienteId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{clienteId}")
    @Operation(summary = "Actualizar cliente")
    public ClienteResponse actualizar(@PathVariable Long clienteId, @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(clienteId, request);
    }

    @PatchMapping("/{clienteId}")
    @Operation(summary = "Actualizar cliente parcialmente")
    public ClienteResponse actualizarParcial(@PathVariable Long clienteId,
                                             @Valid @RequestBody ClientePatchRequest request) {
        return clienteService.actualizarParcial(clienteId, request);
    }

    @DeleteMapping("/{clienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar cliente")
    public void eliminar(@PathVariable Long clienteId) {
        clienteService.eliminar(clienteId);
    }
}
