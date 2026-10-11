import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { CrudApi } from '../../shared/data/crud-store';
import { API_BASE_URL } from './api-base-url';
import { ClientesApi } from './clientes-api';
import { CuentasApi } from './cuentas-api';
import { MovimientosApi } from './movimientos-api';

describe.each<[string, Type<CrudApi<object, object>>, string]>([
  ['ClientesApi', ClientesApi, 'clientes'],
  ['CuentasApi', CuentasApi, 'cuentas'],
  ['MovimientosApi', MovimientosApi, 'movimientos'],
])('%s', (_name, service, resource) => {
  let api: CrudApi<object, object>;
  let http: HttpTestingController;

  function setUp(baseUrl?: string): void {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ...(baseUrl ? [{ provide: API_BASE_URL, useValue: baseUrl }] : []),
      ],
    });
    api = TestBed.inject(service);
    http = TestBed.inject(HttpTestingController);
  }

  afterEach(() => http.verify());

  it('listar hace GET', () => {
    setUp();
    let result: object[] | undefined;

    api.listar().subscribe((items) => (result = items));
    const request = http.expectOne(`/api/${resource}`);
    request.flush([{ id: 1 }]);

    expect(request.request.method).toBe('GET');
    expect(result).toEqual([{ id: 1 }]);
  });

  it('crear hace POST con el body', () => {
    setUp();
    const body = { nombre: 'Nuevo' };

    api.crear(body).subscribe();
    const request = http.expectOne(`/api/${resource}`);
    request.flush({ id: 9 });

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
  });

  it('actualizar hace PUT al id', () => {
    setUp();
    const body = { nombre: 'Editado' };

    api.actualizar(7, body).subscribe();
    const request = http.expectOne(`/api/${resource}/7`);
    request.flush({ id: 7 });

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
  });

  it('eliminar hace DELETE al id', () => {
    setUp();
    let completed = false;

    api.eliminar(7).subscribe({ complete: () => (completed = true) });
    const request = http.expectOne(`/api/${resource}/7`);
    request.flush(null, { status: 204, statusText: 'No Content' });

    expect(request.request.method).toBe('DELETE');
    expect(completed).toBe(true);
  });

  it('usa la URL base configurada', () => {
    setUp('https://banco.example/api/v1');

    api.listar().subscribe();

    http.expectOne(`https://banco.example/api/v1/${resource}`).flush([]);
  });
});
