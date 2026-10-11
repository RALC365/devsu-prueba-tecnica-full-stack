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
import { CLIENTES, CUENTAS } from '../../../../testing/fixtures';
import { ClientesApi } from '../../../core/api/clientes-api';
import { CuentasApi } from '../../../core/api/cuentas-api';
import { ApiError } from '../../../core/http/api-error';
import { Cuenta, CuentaRequest } from '../../../core/models';
import { ToastService } from '../../../core/notifications/toast-service';
import { CuentasPage } from './cuentas-page';

describe('CuentasPage', () => {
  const api = {
    listar: jest.fn<Observable<Cuenta[]>, []>(),
    crear: jest.fn<Observable<Cuenta>, [CuentaRequest]>(),
    actualizar: jest.fn<Observable<Cuenta>, [number, CuentaRequest]>(),
    eliminar: jest.fn<Observable<void>, [number]>(),
  };
  const clientesApi = { listar: jest.fn(() => of(CLIENTES)) };
  const toast = { success: jest.fn(), error: jest.fn() };
  let fixture: ComponentFixture<CuentasPage>;

  const stable = () => fixture.whenStable();

  beforeEach(async () => {
    api.listar.mockReturnValue(of(CUENTAS));
    TestBed.configureTestingModule({
      providers: [
        { provide: CuentasApi, useValue: api },
        { provide: ClientesApi, useValue: clientesApi },
        { provide: ToastService, useValue: toast },
      ],
    });
    fixture = TestBed.createComponent(CuentasPage);
    await stable();
  });

  it('lista las cuentas', () => {
    expect(tableRows(fixture)).toEqual([
      ['478758', 'Ahorros', 'José Lema', '$2,000.00', '$1,425.00', 'Activa', ''],
      ['225487', 'Corriente', 'Marianela Montalvo', '$100.00', '$700.00', 'Activa', ''],
      ['495878', 'Ahorros', 'Juan Osorio', '$0.00', '$150.00', 'Inactiva', ''],
    ]);
  });

  it('filtra por cualquier columna, también por montos', async () => {
    const search = query<HTMLInputElement>(fixture, 'input[type="search"]');

    typeInto(search, 'corriente');
    await stable();
    expect(tableRows(fixture).map((cells) => cells[0])).toEqual(['225487']);

    typeInto(search, '$1,425');
    await stable();
    expect(tableRows(fixture).map((cells) => cells[0])).toEqual(['478758']);
  });

  it('carga los clientes al abrir el modal y crea la cuenta', async () => {
    api.crear.mockReturnValue(of(CUENTAS[0]));
    expect(clientesApi.listar).not.toHaveBeenCalled();

    buttonByText(fixture, 'Nueva cuenta').click();
    await stable();
    expect(clientesApi.listar).toHaveBeenCalledTimes(1);

    selectOption(query(fixture, '#cuenta-cliente'), 'Marianela Montalvo - 1723456789');
    typeInto(query(fixture, '#cuenta-numero'), '585545');
    selectOption(query(fixture, '#cuenta-tipo'), 'Corriente');
    typeInto(query(fixture, '#cuenta-saldo'), '1000');
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(api.crear).toHaveBeenCalledWith({
      numeroCuenta: '585545',
      tipoCuenta: 'CORRIENTE',
      saldoInicial: 1000,
      estado: true,
      clienteId: 2,
    });
    expect(toast.success).toHaveBeenCalledWith('Cuenta creada');
  });

  it('muestra el error de cliente inactivo', async () => {
    api.crear.mockReturnValue(
      throwError(
        () =>
          new ApiError(
            422,
            'No se puede crear una cuenta para un cliente inactivo',
            'CLIENTE_INACTIVO',
          ),
      ),
    );
    buttonByText(fixture, 'Nueva cuenta').click();
    await stable();

    selectOption(query(fixture, '#cuenta-cliente'), 'José Lema');
    typeInto(query(fixture, '#cuenta-numero'), '585545');
    selectOption(query(fixture, '#cuenta-tipo'), 'Ahorros');
    typeInto(query(fixture, '#cuenta-saldo'), '0');
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(textOf(fixture, 'app-modal [role="alert"]')).toBe(
      'No se puede crear una cuenta para un cliente inactivo',
    );
  });

  it('muestra el error al cambiar el titular', async () => {
    api.actualizar.mockReturnValue(
      throwError(
        () =>
          new ApiError(
            422,
            'No se puede cambiar el titular de una cuenta con movimientos registrados',
            'OPERACION_NO_PERMITIDA',
          ),
      ),
    );
    query<HTMLButtonElement>(fixture, 'button[aria-label="Editar cuenta 478758"]').click();
    await stable();

    selectOption(query(fixture, '#cuenta-cliente'), 'Marianela Montalvo');
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(api.actualizar).toHaveBeenCalledWith(1, expect.objectContaining({ clienteId: 2 }));
    expect(textOf(fixture, 'app-modal [role="alert"]')).toBe(
      'No se puede cambiar el titular de una cuenta con movimientos registrados',
    );
    expect(toast.error).toHaveBeenCalledWith('No se pudo guardar la cuenta');
  });

  it('muestra por qué no se puede eliminar la cuenta', async () => {
    api.eliminar.mockReturnValue(
      throwError(
        () =>
          new ApiError(409, 'No se puede eliminar la cuenta porque tiene movimientos registrados'),
      ),
    );

    query<HTMLButtonElement>(fixture, 'button[aria-label="Eliminar cuenta 478758"]').click();
    await stable();
    expect(textOf(fixture, 'app-confirm-dialog')).toContain('¿Desea eliminar la cuenta 478758?');

    buttonByText(fixture, 'Eliminar').click();
    await stable();

    expect(api.eliminar).toHaveBeenCalledWith(1);
    expect(toast.error).toHaveBeenCalledWith(
      'No se puede eliminar la cuenta porque tiene movimientos registrados',
    );
    expect(queryAll(fixture, 'app-confirm-dialog')).toHaveLength(0);
  });
});
