import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ApiError } from './api-error';
import {
  CONNECTION_ERROR_MESSAGE,
  REQUEST_ERROR_MESSAGE,
  SERVER_ERROR_MESSAGE,
  errorInterceptor,
} from './error-interceptor';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  function failingRequest(respond: (url: string) => void): unknown {
    let received: unknown;
    http.get('/api/movimientos').subscribe({ error: (error: unknown) => (received = error) });
    respond('/api/movimientos');
    return received;
  }

  it('convierte un problem+json en ApiError', () => {
    const error = failingRequest((url) =>
      controller.expectOne(url).flush(
        {
          type: 'about:blank',
          title: 'Regla de negocio incumplida',
          status: 422,
          detail: 'Saldo no disponible',
          codigo: 'SALDO_NO_DISPONIBLE',
        },
        { status: 422, statusText: 'Unprocessable Entity' },
      ),
    );

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 422,
      message: 'Saldo no disponible',
      codigo: 'SALDO_NO_DISPONIBLE',
      fieldErrors: {},
    });
  });

  it('mapea los errores por campo', () => {
    const error = failingRequest((url) =>
      controller.expectOne(url).flush(
        {
          status: 400,
          detail: 'La solicitud contiene datos inválidos',
          codigo: 'VALIDACION',
          errores: { valor: 'El valor debe ser mayor a 0', numeroCuenta: 123 },
        },
        { status: 400, statusText: 'Bad Request' },
      ),
    );

    expect(error).toMatchObject({
      status: 400,
      message: 'La solicitud contiene datos inválidos',
      codigo: 'VALIDACION',
      fieldErrors: { valor: 'El valor debe ser mayor a 0' },
    });
  });

  it('status 0 da error de conexión', () => {
    const error = failingRequest((url) =>
      controller.expectOne(url).error(new ProgressEvent('error'), { status: 0 }),
    );

    expect(error).toMatchObject({ status: 0, message: CONNECTION_ERROR_MESSAGE, fieldErrors: {} });
  });

  it('5xx sin detalle usa el mensaje genérico', () => {
    const error = failingRequest((url) =>
      controller.expectOne(url).flush(null, { status: 500, statusText: 'Internal Server Error' }),
    );

    expect(error).toMatchObject({ status: 500, message: SERVER_ERROR_MESSAGE });
    expect((error as ApiError).codigo).toBeUndefined();
  });

  it('4xx sin detalle usa el title o el mensaje genérico', () => {
    const withTitle = failingRequest((url) =>
      controller
        .expectOne(url)
        .flush({ title: 'Not Found' }, { status: 404, statusText: 'Not Found' }),
    );
    const withHtmlBody = failingRequest((url) =>
      controller
        .expectOne(url)
        .flush('<html>Conflict</html>', { status: 409, statusText: 'Conflict' }),
    );

    expect(withTitle).toMatchObject({ status: 404, message: 'Not Found' });
    expect(withHtmlBody).toMatchObject({ status: 409, message: REQUEST_ERROR_MESSAGE });
  });

  it('no toca las respuestas exitosas', () => {
    let body: unknown;

    http.get('/api/clientes').subscribe((response) => (body = response));
    controller.expectOne('/api/clientes').flush([{ clienteId: 1 }]);

    expect(body).toEqual([{ clienteId: 1 }]);
  });
});
