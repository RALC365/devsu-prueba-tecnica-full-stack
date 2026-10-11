package com.devsu.banco.service;

import com.devsu.banco.dto.cliente.ClientePatchRequest;
import com.devsu.banco.dto.cliente.ClienteRequest;
import com.devsu.banco.dto.cliente.ClienteResponse;
import java.util.List;

public interface ClienteService {

    List<ClienteResponse> listar();

    ClienteResponse obtener(Long clienteId);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long clienteId, ClienteRequest request);

    ClienteResponse actualizarParcial(Long clienteId, ClientePatchRequest request);

    void eliminar(Long clienteId);
}
