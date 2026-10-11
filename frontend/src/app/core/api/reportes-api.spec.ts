import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { REPORTE } from '../../../testing/fixtures';
import { Reporte, ReporteFiltro, ReportePdf } from '../models';
import { ReportesApi } from './reportes-api';

describe('ReportesApi', () => {
  const filtro: ReporteFiltro = { clienteId: 2, fechaInicio: '2022-02-01', fechaFin: '2022-02-28' };
  let api: ReportesApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(ReportesApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('genera el reporte con los parámetros del filtro', () => {
    let result: Reporte | undefined;

    api.generar(filtro).subscribe((reporte) => (result = reporte));
    const request = http.expectOne((req) => req.url === '/api/reportes');
    request.flush(REPORTE);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('clienteId')).toBe('2');
    expect(request.request.params.get('fechaInicio')).toBe('2022-02-01');
    expect(request.request.params.get('fechaFin')).toBe('2022-02-28');
    expect(result).toEqual(REPORTE);
  });

  it('pide el PDF con los mismos parámetros', () => {
    const pdf: ReportePdf = {
      nombreArchivo: 'estado-cuenta-2-20220201-20220228.pdf',
      tipoContenido: 'application/pdf',
      contenidoBase64: 'JVBERi0=',
    };
    let result: ReportePdf | undefined;

    api.descargarPdf(filtro).subscribe((response) => (result = response));
    const request = http.expectOne(
      '/api/reportes/pdf?clienteId=2&fechaInicio=2022-02-01&fechaFin=2022-02-28',
    );
    request.flush(pdf);

    expect(request.request.method).toBe('GET');
    expect(result).toEqual(pdf);
  });
});
