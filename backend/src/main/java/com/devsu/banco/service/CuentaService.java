package com.devsu.banco.service;

import com.devsu.banco.dto.cuenta.CuentaPatchRequest;
import com.devsu.banco.dto.cuenta.CuentaRequest;
import com.devsu.banco.dto.cuenta.CuentaResponse;
import java.util.List;

public interface CuentaService {

    List<CuentaResponse> listar(Long clienteId);

    CuentaResponse obtener(Long cuentaId);

    CuentaResponse crear(CuentaRequest request);

    CuentaResponse actualizar(Long cuentaId, CuentaRequest request);

    CuentaResponse actualizarParcial(Long cuentaId, CuentaPatchRequest request);

    void eliminar(Long cuentaId);
}
