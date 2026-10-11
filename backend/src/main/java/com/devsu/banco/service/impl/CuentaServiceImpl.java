package com.devsu.banco.service.impl;

import com.devsu.banco.dto.cuenta.CuentaPatchRequest;
import com.devsu.banco.dto.cuenta.CuentaRequest;
import com.devsu.banco.dto.cuenta.CuentaResponse;
import com.devsu.banco.entity.Cliente;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.ConflictoDatosException;
import com.devsu.banco.exception.RecursoDuplicadoException;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.mapper.CuentaMapper;
import com.devsu.banco.repository.ClienteRepository;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.CuentaService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoRepository movimientoRepository;
    private final CuentaMapper cuentaMapper;
    private final RecalculadorSaldos recalculadorSaldos;

    @Override
    public List<CuentaResponse> listar(Long clienteId) {
        List<Cuenta> cuentas = clienteId == null
                ? cuentaRepository.findAllByOrderByNumeroCuentaAsc()
                : cuentaRepository.findByClienteIdOrderByNumeroCuentaAsc(clienteId);
        return cuentas.stream().map(cuentaMapper::toResponse).toList();
    }

    @Override
    public CuentaResponse obtener(Long cuentaId) {
        return cuentaRepository.findWithClienteById(cuentaId)
                .map(cuentaMapper::toResponse)
                .orElseThrow(() -> cuentaNoEncontrada(cuentaId));
    }

    @Override
    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        validarNumeroDisponible(request.numeroCuenta(), null);
        Cuenta cuenta = cuentaMapper.toEntity(request, buscarTitularActivo(request.clienteId()));
        cuenta.inicializarSaldoDisponible();
        Cuenta guardada = cuentaRepository.save(cuenta);
        log.info("Cuenta {} creada para el cliente {}", guardada.getId(), request.clienteId());
        return cuentaMapper.toResponse(guardada);
    }

    @Override
    @Transactional
    public CuentaResponse actualizar(Long cuentaId, CuentaRequest request) {
        Cuenta cuenta = bloquear(cuentaId);
        validarNumeroDisponible(request.numeroCuenta(), cuentaId);
        BigDecimal saldoInicialAnterior = cuenta.getSaldoInicial();
        cuentaMapper.actualizar(cuenta, request);
        cambiarTitularSiCorresponde(cuenta, request.clienteId());
        recalcularSiCambioSaldoInicial(cuenta, saldoInicialAnterior);
        return cuentaMapper.toResponse(cuenta);
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(Long cuentaId, CuentaPatchRequest request) {
        Cuenta cuenta = bloquear(cuentaId);
        if (request.numeroCuenta() != null) {
            validarNumeroDisponible(request.numeroCuenta(), cuentaId);
        }
        BigDecimal saldoInicialAnterior = cuenta.getSaldoInicial();
        cuentaMapper.actualizarParcial(cuenta, request);
        if (request.clienteId() != null) {
            cambiarTitularSiCorresponde(cuenta, request.clienteId());
        }
        recalcularSiCambioSaldoInicial(cuenta, saldoInicialAnterior);
        return cuentaMapper.toResponse(cuenta);
    }

    @Override
    @Transactional
    public void eliminar(Long cuentaId) {
        Cuenta cuenta = bloquear(cuentaId);
        if (movimientoRepository.existsByCuentaId(cuentaId)) {
            throw new ConflictoDatosException("No se puede eliminar la cuenta porque tiene movimientos registrados");
        }
        cuentaRepository.delete(cuenta);
        log.info("Cuenta {} eliminada", cuentaId);
    }

    private Cuenta bloquear(Long cuentaId) {
        return cuentaRepository.findByIdForUpdate(cuentaId).orElseThrow(() -> cuentaNoEncontrada(cuentaId));
    }

    private Cliente buscarTitularActivo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + clienteId));
        if (!cliente.isActivo()) {
            throw new ReglaNegocioException(CodigoError.CLIENTE_INACTIVO,
                    "El cliente %s está inactivo".formatted(cliente.getNombre()));
        }
        return cliente;
    }

    private void cambiarTitularSiCorresponde(Cuenta cuenta, Long clienteId) {
        if (clienteId.equals(cuenta.getCliente().getId())) {
            return;
        }
        if (movimientoRepository.existsByCuentaId(cuenta.getId())) {
            throw new ReglaNegocioException(CodigoError.OPERACION_NO_PERMITIDA,
                    "No se puede cambiar el titular de una cuenta con movimientos registrados");
        }
        cuenta.setCliente(buscarTitularActivo(clienteId));
    }

    private void recalcularSiCambioSaldoInicial(Cuenta cuenta, BigDecimal saldoInicialAnterior) {
        if (cuenta.getSaldoInicial().compareTo(saldoInicialAnterior) != 0) {
            recalculadorSaldos.recalcular(cuenta);
        }
    }

    private void validarNumeroDisponible(String numeroCuenta, Long cuentaId) {
        boolean enUso = cuentaId == null
                ? cuentaRepository.existsByNumeroCuenta(numeroCuenta)
                : cuentaRepository.existsByNumeroCuentaAndIdNot(numeroCuenta, cuentaId);
        if (enUso) {
            throw new RecursoDuplicadoException("Ya existe una cuenta con el número " + numeroCuenta);
        }
    }

    private static RecursoNoEncontradoException cuentaNoEncontrada(Long cuentaId) {
        return new RecursoNoEncontradoException("No existe una cuenta con id " + cuentaId);
    }
}
