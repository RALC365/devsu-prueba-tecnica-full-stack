import { Injectable, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';

import { ClientesApi } from '../../core/api/clientes-api';
import { CuentasApi } from '../../core/api/cuentas-api';
import {
  Cuenta,
  CuentaRequest,
  ESTADO_CUENTA,
  TIPO_CUENTA_LABELS,
  estadoLabel,
} from '../../core/models';
import { CrudStore } from '../../shared/data/crud-store';
import { toLookup } from '../../shared/data/lookup';
import { formatMoney } from '../../shared/utils/formatters';

@Injectable()
export class CuentasStore extends CrudStore<Cuenta, CuentaRequest> {
  private readonly clientesApi = inject(ClientesApi);

  // se recarga cada vez que se abre el modal para tener los clientes al día
  readonly clientes = toLookup(
    rxResource({
      params: () => this.editorOpen() || undefined,
      stream: () => this.clientesApi.listar(),
      defaultValue: [],
    }),
  );

  constructor() {
    super(inject(CuentasApi), {
      idOf: (cuenta) => cuenta.cuentaId,
      searchableValues: (cuenta) => [
        cuenta.numeroCuenta,
        TIPO_CUENTA_LABELS[cuenta.tipoCuenta],
        cuenta.cliente,
        formatMoney(cuenta.saldoInicial),
        cuenta.saldoInicial,
        formatMoney(cuenta.saldoDisponible),
        cuenta.saldoDisponible,
        estadoLabel(cuenta.estado, ESTADO_CUENTA),
      ],
      messages: {
        created: 'Cuenta creada',
        updated: 'Cuenta actualizada',
        deleted: 'Cuenta eliminada',
        saveFailed: 'No se pudo guardar la cuenta',
      },
    });
  }
}
