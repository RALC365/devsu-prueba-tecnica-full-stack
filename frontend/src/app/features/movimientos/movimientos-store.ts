import { Injectable, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';

import { CuentasApi } from '../../core/api/cuentas-api';
import { MovimientosApi } from '../../core/api/movimientos-api';
import {
  Movimiento,
  MovimientoRequest,
  TIPO_CUENTA_LABELS,
  TIPO_MOVIMIENTO_LABELS,
} from '../../core/models';
import { CrudStore } from '../../shared/data/crud-store';
import { toLookup } from '../../shared/data/lookup';
import { formatDateTime, formatMoney, formatSignedMoney } from '../../shared/utils/formatters';

@Injectable()
export class MovimientosStore extends CrudStore<Movimiento, MovimientoRequest> {
  private readonly cuentasApi = inject(CuentasApi);

  readonly cuentas = toLookup(
    rxResource({
      params: () => this.editorOpen() || undefined,
      stream: () => this.cuentasApi.listar(),
      defaultValue: [],
    }),
  );

  constructor() {
    super(inject(MovimientosApi), {
      idOf: (movimiento) => movimiento.movimientoId,
      searchableValues: (movimiento) => [
        formatDateTime(movimiento.fecha),
        movimiento.numeroCuenta,
        TIPO_CUENTA_LABELS[movimiento.tipoCuenta],
        movimiento.cliente,
        TIPO_MOVIMIENTO_LABELS[movimiento.tipoMovimiento],
        formatSignedMoney(movimiento.valor),
        Math.abs(movimiento.valor),
        formatMoney(movimiento.saldo),
        movimiento.saldo,
      ],
      messages: {
        created: 'Movimiento registrado',
        updated: 'Movimiento actualizado',
        deleted: 'Movimiento eliminado',
        saveFailed: 'No se pudo guardar el movimiento',
      },
    });
  }
}
