import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';

import {
  buttonByText,
  query,
  queryAll,
  selectOption,
  tableRows,
  textOf,
  typeInto,
} from '../../../../testing/dom';
import { CUENTAS, MOVIMIENTOS } from '../../../../testing/fixtures';
import { CuentasApi } from '../../../core/api/cuentas-api';
import { MovimientosApi } from '../../../core/api/movimientos-api';
import { ApiError } from '../../../core/http/api-error';
import { Movimiento, MovimientoRequest } from '../../../core/models';
import { ToastService } from '../../../core/notifications/toast-service';
import { MovimientosPage } from './movimientos-page';

describe('MovimientosPage', () => {
  const api = {
    listar: jest.fn<Observable<Movimiento[]>, []>(),
    crear: jest.fn<Observable<Movimiento>, [MovimientoRequest]>(),
    actualizar: jest.fn<Observable<Movimiento>, [number, MovimientoRequest]>(),
    eliminar: jest.fn<Observable<void>, [number]>(),
  };
  const cuentasApi = { listar: jest.fn(() => of(CUENTAS)) };
  const toast = { success: jest.fn(), error: jest.fn() };
  let fixture: ComponentFixture<MovimientosPage>;

  const stable = () => fixture.whenStable();

  beforeEach(async () => {
    api.listar.mockReturnValue(of(MOVIMIENTOS));
    TestBed.configureTestingModule({
      providers: [
        { provide: MovimientosApi, useValue: api },
        { provide: CuentasApi, useValue: cuentasApi },
        { provide: ToastService, useValue: toast },
      ],
    });
    fixture = TestBed.createComponent(MovimientosPage);
    await stable();
  });

  it('lista los movimientos', () => {
    expect(tableRows(fixture)).toEqual([
      [
        '10/02/2022 14:30',
        '225487',
        'Corriente',
        'Marianela Montalvo',
        'Crédito',
        '+$600.00',
        '$700.00',
        '',
      ],
      ['08/02/2022 09:05', '478758', 'Ahorros', 'José Lema', 'Débito', '-$575.00', '$1,425.00', ''],
    ]);
    const amounts = queryAll(fixture, 'tbody td:nth-child(6)');
    expect(amounts[0].classList).toContain('amount--credit');
    expect(amounts[1].classList).toContain('amount--debit');
  });

  it('filtra por fecha, tipo o valor', async () => {
    const search = query<HTMLInputElement>(fixture, 'input[type="search"]');

    typeInto(search, '08/02/2022');
    await stable();
    expect(tableRows(fixture).map((cells) => cells[1])).toEqual(['478758']);

    typeInto(search, 'credito 600');
    await stable();
    expect(tableRows(fixture).map((cells) => cells[1])).toEqual(['225487']);
  });

  it('muestra saldo no disponible', async () => {
    api.crear.mockReturnValue(
      throwError(() => new ApiError(422, 'Saldo no disponible', 'SALDO_NO_DISPONIBLE')),
    );
    buttonByText(fixture, 'Nuevo movimiento').click();
    await stable();

    selectOption(query(fixture, '#movimiento-cuenta'), '478758');
    selectOption(query(fixture, '#movimiento-tipo'), 'Retiro');
    typeInto(query(fixture, '#movimiento-valor'), '200');
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(api.crear).toHaveBeenCalledWith({
      numeroCuenta: '478758',
      tipoMovimiento: 'DEBITO',
      valor: 200,
    });
    expect(textOf(fixture, 'app-modal [role="alert"]')).toBe('Saldo no disponible');
    expect(toast.error).toHaveBeenCalledWith('No se pudo guardar el movimiento');
  });

  it('muestra el error de cliente inactivo', async () => {
    api.crear.mockReturnValue(
      throwError(
        () =>
          new ApiError(
            422,
            'No se pueden registrar movimientos de un cliente inactivo',
            'CLIENTE_INACTIVO',
          ),
      ),
    );
    api.eliminar.mockReturnValue(
      throwError(
        () =>
          new ApiError(
            422,
            'No se pueden eliminar movimientos de un cliente inactivo',
            'CLIENTE_INACTIVO',
          ),
      ),
    );

    buttonByText(fixture, 'Nuevo movimiento').click();
    await stable();
    selectOption(query(fixture, '#movimiento-cuenta'), '225487');
    selectOption(query(fixture, '#movimiento-tipo'), 'Depósito');
    typeInto(query(fixture, '#movimiento-valor'), '50');
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(textOf(fixture, 'app-modal [role="alert"]')).toBe(
      'No se pueden registrar movimientos de un cliente inactivo',
    );

    buttonByText(fixture, 'Cancelar').click();
    await stable();
    query<HTMLButtonElement>(
      fixture,
      'button[aria-label="Eliminar movimiento de la cuenta 225487"]',
    ).click();
    await stable();
    buttonByText(fixture, 'Eliminar').click();
    await stable();

    expect(toast.error).toHaveBeenCalledWith(
      'No se pueden eliminar movimientos de un cliente inactivo',
    );
    expect(queryAll(fixture, 'app-confirm-dialog')).toHaveLength(0);
  });

  it('edita un movimiento', async () => {
    api.actualizar.mockReturnValue(of(MOVIMIENTOS[1]));

    query<HTMLButtonElement>(
      fixture,
      'button[aria-label="Editar movimiento de la cuenta 478758"]',
    ).click();
    await stable();

    expect(query<HTMLSelectElement>(fixture, '#movimiento-cuenta').disabled).toBe(true);
    typeInto(query(fixture, '#movimiento-valor'), '500');
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(api.actualizar).toHaveBeenCalledWith(11, {
      numeroCuenta: '478758',
      tipoMovimiento: 'DEBITO',
      valor: 500,
    });
    expect(toast.success).toHaveBeenCalledWith('Movimiento actualizado');
  });

  it('elimina un movimiento', async () => {
    api.eliminar.mockReturnValue(of(undefined));

    query<HTMLButtonElement>(
      fixture,
      'button[aria-label="Eliminar movimiento de la cuenta 225487"]',
    ).click();
    await stable();
    expect(textOf(fixture, 'app-confirm-dialog')).toContain(
      '¿Desea eliminar este movimiento? Se recalcularán los saldos de la cuenta.',
    );

    buttonByText(fixture, 'Eliminar').click();
    await stable();

    expect(api.eliminar).toHaveBeenCalledWith(10);
    expect(toast.success).toHaveBeenCalledWith('Movimiento eliminado');
    expect(api.listar).toHaveBeenCalledTimes(2);
  });
});
