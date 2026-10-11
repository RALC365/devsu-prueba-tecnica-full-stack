package com.devsu.banco.service.impl;

import com.devsu.banco.dto.movimiento.MovimientoPatchRequest;
import com.devsu.banco.dto.movimiento.MovimientoRequest;
import com.devsu.banco.dto.movimiento.MovimientoResponse;
import com.devsu.banco.entity.Cuenta;
import com.devsu.banco.entity.Movimiento;
import com.devsu.banco.entity.TipoMovimiento;
import com.devsu.banco.exception.CodigoError;
import com.devsu.banco.exception.RecursoNoEncontradoException;
import com.devsu.banco.exception.ReglaNegocioException;
import com.devsu.banco.mapper.MovimientoMapper;
import com.devsu.banco.repository.CuentaRepository;
import com.devsu.banco.repository.MovimientoRepository;
import com.devsu.banco.service.MovimientoService;
import com.devsu.banco.service.strategy.MovimientoStrategy;
import com.devsu.banco.service.strategy.MovimientoStrategyFactory;
import com.devsu.banco.service.strategy.OperacionMovimiento;
import com.devsu.banco.util.Montos;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoStrategyFactory estrategias;
    private final RecalculadorSaldos recalculadorSaldos;
    private final MovimientoMapper movimientoMapper;
    private final Clock clock;

    @Override
    public List<MovimientoResponse> listar(String numeroCuenta) {
        List<Movimiento> movimientos = StringUtils.hasText(numeroCuenta)
                ? movimientoRepository.findByCuentaNumeroCuentaOrderByFechaDescIdDesc(numeroCuenta)
                : movimientoRepository.findAllByOrderByFechaDescIdDesc();
        return movimientos.stream().map(movimientoMapper::toResponse).toList();
    }

    @Override
    public MovimientoResponse obtener(Long movimientoId) {
        return movimientoRepository.findWithCuentaById(movimientoId)
                .map(movimientoMapper::toResponse)
                .orElseThrow(() -> movimientoNoEncontrado(movimientoId));
    }

    @Override
    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuentaForUpdate(request.numeroCuenta())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cuenta " + request.numeroCuenta()));
        validarCuentaOperativa(cuenta);

        LocalDateTime fecha = LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
        BigDecimal monto = Montos.normalizar(request.valor());
        MovimientoStrategy estrategia = estrategias.obtener(request.tipoMovimiento());
        estrategia.validar(OperacionMovimiento.nueva(cuenta, monto, fecha.toLocalDate()));

        BigDecimal valor = estrategia.aplicarSigno(monto);
        BigDecimal saldo = cuenta.aplicarMovimiento(valor);
        Movimiento movimiento = movimientoRepository.save(
                Movimiento.registrar(cuenta, request.tipoMovimiento(), valor, saldo, fecha));
        log.info("Movimiento {} ({}) registrado en la cuenta {}", movimiento.getId(), request.tipoMovimiento(), cuenta.getId());
        log.debug("Movimiento {}: valor {}, saldo disponible {}", movimiento.getId(), valor, saldo);
        return movimientoMapper.toResponse(movimiento);
    }

    @Override
    @Transactional
    public MovimientoResponse actualizar(Long movimientoId, MovimientoRequest request) {
        Movimiento movimiento = buscarConCuentaBloqueada(movimientoId);
        if (!movimiento.getCuenta().getNumeroCuenta().equals(request.numeroCuenta())) {
            throw new ReglaNegocioException(CodigoError.OPERACION_NO_PERMITIDA,
                    "No se puede cambiar la cuenta de un movimiento");
        }
        return corregir(movimiento, request.tipoMovimiento(), request.valor());
    }

    @Override
    @Transactional
    public MovimientoResponse actualizarParcial(Long movimientoId, MovimientoPatchRequest request) {
        Movimiento movimiento = buscarConCuentaBloqueada(movimientoId);
        TipoMovimiento tipo = Objects.requireNonNullElse(request.tipoMovimiento(), movimiento.getTipoMovimiento());
        BigDecimal monto = Objects.requireNonNullElseGet(request.valor(), () -> movimiento.getValor().abs());
        return corregir(movimiento, tipo, monto);
    }

    @Override
    @Transactional
    public void eliminar(Long movimientoId) {
        Movimiento movimiento = buscarConCuentaBloqueada(movimientoId);
        Cuenta cuenta = movimiento.getCuenta();
        validarCuentaOperativa(cuenta);
        movimientoRepository.delete(movimiento);
        recalculadorSaldos.recalcular(cuenta);
        log.info("Movimiento {} eliminado de la cuenta {}", movimientoId, cuenta.getId());
    }

    private MovimientoResponse corregir(Movimiento movimiento, TipoMovimiento tipo, BigDecimal montoSolicitado) {
        Cuenta cuenta = movimiento.getCuenta();
        validarCuentaOperativa(cuenta);
        BigDecimal monto = Montos.normalizar(montoSolicitado);
        MovimientoStrategy estrategia = estrategias.obtener(tipo);
        estrategia.validar(OperacionMovimiento.correccion(movimiento, monto));
        movimiento.corregir(tipo, estrategia.aplicarSigno(monto));
        recalculadorSaldos.recalcular(cuenta);
        log.info("Movimiento {} corregido ({})", movimiento.getId(), tipo);
        log.debug("Movimiento {}: nuevo monto {}", movimiento.getId(), monto);
        return movimientoMapper.toResponse(movimiento);
    }

    // primero se bloquea la cuenta para que el movimiento se cargue con el saldo actual
    private Movimiento buscarConCuentaBloqueada(Long movimientoId) {
        Long cuentaId = movimientoRepository.findCuentaIdById(movimientoId)
                .orElseThrow(() -> movimientoNoEncontrado(movimientoId));
        cuentaRepository.findByIdForUpdate(cuentaId)
                .orElseThrow(() -> movimientoNoEncontrado(movimientoId));
        return movimientoRepository.findWithCuentaById(movimientoId)
                .orElseThrow(() -> movimientoNoEncontrado(movimientoId));
    }

    private static void validarCuentaOperativa(Cuenta cuenta) {
        if (!cuenta.isActiva()) {
            throw new ReglaNegocioException(CodigoError.CUENTA_INACTIVA,
                    "La cuenta %s está inactiva".formatted(cuenta.getNumeroCuenta()));
        }
        if (!cuenta.getCliente().isActivo()) {
            throw new ReglaNegocioException(CodigoError.CLIENTE_INACTIVO,
                    "El cliente %s está inactivo".formatted(cuenta.getCliente().getNombre()));
        }
    }

    private static RecursoNoEncontradoException movimientoNoEncontrado(Long movimientoId) {
        return new RecursoNoEncontradoException("No existe un movimiento con id " + movimientoId);
    }
}
