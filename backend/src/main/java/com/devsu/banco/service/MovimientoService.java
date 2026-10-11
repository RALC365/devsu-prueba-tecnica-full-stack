package com.devsu.banco.service;

import com.devsu.banco.dto.movimiento.MovimientoPatchRequest;
import com.devsu.banco.dto.movimiento.MovimientoRequest;
import com.devsu.banco.dto.movimiento.MovimientoResponse;
import java.util.List;

public interface MovimientoService {

    List<MovimientoResponse> listar(String numeroCuenta);

    MovimientoResponse obtener(Long movimientoId);

    MovimientoResponse registrar(MovimientoRequest request);

    MovimientoResponse actualizar(Long movimientoId, MovimientoRequest request);

    MovimientoResponse actualizarParcial(Long movimientoId, MovimientoPatchRequest request);

    void eliminar(Long movimientoId);
}
