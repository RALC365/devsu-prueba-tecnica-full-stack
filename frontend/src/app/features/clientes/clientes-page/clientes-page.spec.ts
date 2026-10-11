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
import { CLIENTES } from '../../../../testing/fixtures';
import { ClientesApi } from '../../../core/api/clientes-api';
import { ApiError } from '../../../core/http/api-error';
import { Cliente, ClienteRequest } from '../../../core/models';
import { ToastService } from '../../../core/notifications/toast-service';
import { ClientesPage } from './clientes-page';

describe('ClientesPage', () => {
  const api = {
    listar: jest.fn<Observable<Cliente[]>, []>(),
    crear: jest.fn<Observable<Cliente>, [ClienteRequest]>(),
    actualizar: jest.fn<Observable<Cliente>, [number, ClienteRequest]>(),
    eliminar: jest.fn<Observable<void>, [number]>(),
  };
  const toast = { success: jest.fn(), error: jest.fn() };
  let fixture: ComponentFixture<ClientesPage>;

  const stable = () => fixture.whenStable();
  const names = () => tableRows(fixture).map((cells) => cells[0]);
  const modal = () => (fixture.nativeElement as HTMLElement).querySelector('app-modal');

  async function render(): Promise<void> {
    TestBed.configureTestingModule({
      providers: [
        { provide: ClientesApi, useValue: api },
        { provide: ToastService, useValue: toast },
      ],
    });
    fixture = TestBed.createComponent(ClientesPage);
    await stable();
  }

  beforeEach(() => api.listar.mockReturnValue(of(CLIENTES)));

  it('lista los clientes', async () => {
    await render();

    expect(tableRows(fixture)).toEqual([
      [
        'José Lema',
        '1712345678',
        'Masculino',
        '35',
        'Otavalo sn y principal',
        '098254785',
        'Activo',
        '',
      ],
      [
        'Marianela Montalvo',
        '1723456789',
        'Femenino',
        '29',
        'Amazonas y NNUU',
        '097548965',
        'Activo',
        '',
      ],
      [
        'Juan Osorio',
        '1734567890',
        'Masculino',
        '41',
        '13 junio y Equinoccial',
        '098874587',
        'Inactivo',
        '',
      ],
    ]);
  });

  it('filtra por texto', async () => {
    await render();

    typeInto(query(fixture, 'input[type="search"]'), 'JOSE');
    await stable();

    expect(names()).toEqual(['José Lema']);
    expect(textOf(fixture, '.quick-search__count')).toBe('1 de 3 registros');

    typeInto(query(fixture, 'input[type="search"]'), 'inactivo');
    await stable();

    expect(names()).toEqual(['Juan Osorio']);
  });

  it('sin resultados permite limpiar la búsqueda', async () => {
    await render();
    typeInto(query(fixture, 'input[type="search"]'), 'zzz');
    await stable();

    expect(textOf(fixture, 'app-table-panel')).toContain('No se encontraron resultados');

    buttonByText(fixture, 'Limpiar búsqueda').click();
    await stable();

    expect(names()).toHaveLength(3);
  });

  it('abre y cierra el modal de alta', async () => {
    await render();

    buttonByText(fixture, 'Nuevo cliente').click();
    await stable();

    expect(textOf(fixture, '.modal__title')).toBe('Nuevo cliente');
    expect(query<HTMLInputElement>(fixture, '#cliente-nombre').value).toBe('');

    buttonByText(fixture, 'Cancelar').click();
    await stable();

    expect(modal()).toBeNull();
  });

  it('abre la edición con los datos del cliente', async () => {
    await render();

    query<HTMLButtonElement>(
      fixture,
      'button[aria-label="Editar cliente Marianela Montalvo"]',
    ).click();
    await stable();

    expect(textOf(fixture, '.modal__title')).toBe('Editar cliente');
    expect(query<HTMLInputElement>(fixture, '#cliente-nombre').value).toBe('Marianela Montalvo');
    expect(query<HTMLInputElement>(fixture, '#cliente-identificacion').value).toBe('1723456789');
  });

  it('crea un cliente', async () => {
    api.crear.mockReturnValue(of({ ...CLIENTES[0], clienteId: 4 }));
    await render();
    buttonByText(fixture, 'Nuevo cliente').click();
    await stable();

    fillForm();
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(api.crear).toHaveBeenCalledWith({
      nombre: 'Ana Pérez',
      genero: 'FEMENINO',
      edad: 30,
      identificacion: '0102030405',
      direccion: 'Av. Amazonas N24',
      telefono: '0991234567',
      contrasena: '1234',
      estado: true,
    });
    expect(toast.success).toHaveBeenCalledWith('Cliente creado');
    expect(modal()).toBeNull();
    expect(api.listar).toHaveBeenCalledTimes(2);
  });

  it('muestra el error del backend si el cliente ya existe', async () => {
    api.crear.mockReturnValue(
      throwError(
        () =>
          new ApiError(
            409,
            'Ya existe un cliente con la identificación 0102030405',
            'RECURSO_DUPLICADO',
          ),
      ),
    );
    await render();
    buttonByText(fixture, 'Nuevo cliente').click();
    await stable();

    fillForm();
    buttonByText(fixture, 'Guardar').click();
    await stable();

    expect(textOf(fixture, 'app-modal [role="alert"]')).toBe(
      'Ya existe un cliente con la identificación 0102030405',
    );
    expect(toast.error).toHaveBeenCalledWith('No se pudo guardar el cliente');
    expect(modal()).not.toBeNull();
  });

  it('elimina un cliente', async () => {
    api.eliminar.mockReturnValue(of(undefined));
    await render();

    query<HTMLButtonElement>(fixture, 'button[aria-label="Eliminar cliente José Lema"]').click();
    await stable();
    expect(textOf(fixture, 'app-confirm-dialog')).toContain('¿Desea eliminar a José Lema?');

    buttonByText(fixture, 'Eliminar').click();
    await stable();

    expect(api.eliminar).toHaveBeenCalledWith(1);
    expect(toast.success).toHaveBeenCalledWith('Cliente eliminado');
    expect(queryAll(fixture, 'app-confirm-dialog')).toHaveLength(0);
    expect(api.listar).toHaveBeenCalledTimes(2);
  });

  it('muestra por qué no se puede eliminar', async () => {
    api.eliminar.mockReturnValue(
      throwError(
        () => new ApiError(409, 'No se puede eliminar el cliente porque tiene cuentas asociadas'),
      ),
    );
    await render();

    query<HTMLButtonElement>(fixture, 'button[aria-label="Eliminar cliente José Lema"]').click();
    await stable();
    buttonByText(fixture, 'Eliminar').click();
    await stable();

    expect(toast.error).toHaveBeenCalledWith(
      'No se puede eliminar el cliente porque tiene cuentas asociadas',
    );
    expect(queryAll(fixture, 'app-confirm-dialog')).toHaveLength(0);
    expect(names()).toHaveLength(3);
  });

  it('permite reintentar si falla la carga', async () => {
    api.listar.mockReturnValueOnce(
      throwError(() => new ApiError(0, 'No se pudo conectar con el servidor')),
    );
    await render();

    expect(textOf(fixture, 'app-table-panel')).toContain('No se pudo conectar con el servidor');

    buttonByText(fixture, 'Reintentar').click();
    await stable();

    expect(names()).toHaveLength(3);
  });

  function fillForm(): void {
    typeInto(query(fixture, '#cliente-nombre'), 'Ana Pérez');
    typeInto(query(fixture, '#cliente-identificacion'), '0102030405');
    selectOption(query(fixture, '#cliente-genero'), 'Femenino');
    typeInto(query(fixture, '#cliente-edad'), '30');
    typeInto(query(fixture, '#cliente-telefono'), '0991234567');
    typeInto(query(fixture, '#cliente-direccion'), 'Av. Amazonas N24');
    typeInto(query(fixture, '#cliente-contrasena'), '1234');
  }
});
