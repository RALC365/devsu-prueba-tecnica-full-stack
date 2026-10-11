package com.devsu.banco.mapper;

import com.devsu.banco.dto.cliente.ClientePatchRequest;
import com.devsu.banco.dto.cliente.ClienteRequest;
import com.devsu.banco.dto.cliente.ClienteResponse;
import com.devsu.banco.entity.Cliente;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente();
        actualizar(cliente, request);
        return cliente;
    }

    public void actualizar(Cliente cliente, ClienteRequest request) {
        cliente.setNombre(request.nombre().trim());
        cliente.setGenero(request.genero());
        cliente.setEdad(request.edad());
        cliente.setIdentificacion(request.identificacion());
        cliente.setDireccion(request.direccion().trim());
        cliente.setTelefono(request.telefono());
        cliente.setEstado(request.estado());
    }

    public void actualizarParcial(Cliente cliente, ClientePatchRequest request) {
        Optional.ofNullable(request.nombre()).map(String::trim).ifPresent(cliente::setNombre);
        Optional.ofNullable(request.genero()).ifPresent(cliente::setGenero);
        Optional.ofNullable(request.edad()).ifPresent(cliente::setEdad);
        Optional.ofNullable(request.identificacion()).ifPresent(cliente::setIdentificacion);
        Optional.ofNullable(request.direccion()).map(String::trim).ifPresent(cliente::setDireccion);
        Optional.ofNullable(request.telefono()).ifPresent(cliente::setTelefono);
        Optional.ofNullable(request.estado()).ifPresent(cliente::setEstado);
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getClienteId(),
                cliente.getNombre(),
                cliente.getGenero(),
                cliente.getEdad(),
                cliente.getIdentificacion(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getEstado());
    }
}
