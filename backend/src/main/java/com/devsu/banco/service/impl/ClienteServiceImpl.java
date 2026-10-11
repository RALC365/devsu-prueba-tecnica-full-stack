package com.devsu.banco.service.impl;

import com.devsu.banco.dto.cliente.ClientePatchRequest;
import com.devsu.banco.dto.cliente.ClienteRequest;
import com.devsu.banco.dto.cliente.ClienteResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.exception.ConflictoDatosException;
import com.devsu.banco.exception.RecursoDuplicadoException;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.mapper.ClienteMapper;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.service.ClienteService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByOrderByNombreAsc().stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Override
    public ClienteResponse obtener(Long clienteId) {
        return clienteMapper.toResponse(buscar(clienteId));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarIdentificacionDisponible(request.identificacion(), null);
        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setContrasena(passwordEncoder.encode(request.contrasena()));
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente {} creado", guardado.getClienteId());
        return clienteMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long clienteId, ClienteRequest request) {
        Cliente cliente = buscar(clienteId);
        validarIdentificacionDisponible(request.identificacion(), clienteId);
        clienteMapper.actualizar(cliente, request);
        // en PUT la contraseña es opcional
        if (request.contrasena() != null) {
            cliente.setContrasena(passwordEncoder.encode(request.contrasena()));
        }
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(Long clienteId, ClientePatchRequest request) {
        Cliente cliente = buscar(clienteId);
        if (request.identificacion() != null) {
            validarIdentificacionDisponible(request.identificacion(), clienteId);
        }
        clienteMapper.actualizarParcial(cliente, request);
        if (request.contrasena() != null) {
            cliente.setContrasena(passwordEncoder.encode(request.contrasena()));
        }
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public void eliminar(Long clienteId) {
        Cliente cliente = buscar(clienteId);
        if (cuentaRepository.existsByClienteId(clienteId)) {
            throw new ConflictoDatosException("No se puede eliminar el cliente porque tiene cuentas asociadas");
        }
        clienteRepository.delete(cliente);
        log.info("Cliente {} eliminado", clienteId);
    }

    private Cliente buscar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + clienteId));
    }

    private void validarIdentificacionDisponible(String identificacion, Long clienteId) {
        boolean enUso = clienteId == null
                ? clienteRepository.existsByIdentificacion(identificacion)
                : clienteRepository.existsByIdentificacionAndIdNot(identificacion, clienteId);
        if (enUso) {
            throw new RecursoDuplicadoException("Ya existe un cliente con la identificación " + identificacion);
        }
    }
}
