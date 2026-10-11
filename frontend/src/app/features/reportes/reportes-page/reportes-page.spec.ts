import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';

import {
  buttonByText,
  query,
  queryAll,
  selectOption,
  textOf,
  typeInto,
} from '../../../../testing/dom';
import { CLIENTES, REPORTE } from '../../../../testing/fixtures';
import { ClientesApi } from '../../../core/api/clientes-api';
import { ReportesApi } from '../../../core/api/reportes-api';
import { ApiError } from '../../../core/http/api-error';
import { Reporte, ReporteFiltro, ReportePdf } from '../../../core/models';
import { ToastService } from '../../../core/notifications/toast-service';
import { ReportesPage } from './reportes-page';

describe('ReportesPage', () => {
  const filtro: ReporteFiltro = { clienteId: 2, fechaInicio: '2022-02-01', fechaFin: '2022-02-28' };
  const reportesApi = {
    generar: jest.fn<Observable<Reporte>, [ReporteFiltro]>(),
    descargarPdf: jest.fn<Observable<ReportePdf>, [ReporteFiltro]>(),
  };
  const clientesApi = { listar: jest.fn(() => of(CLIENTES)) };
  const toast = { success: jest.fn(), error: jest.fn() };
  let fixture: ComponentFixture<ReportesPage>;

  const stable = () => fixture.whenStable();
  const page = () => fixture.nativeElement as HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: ReportesApi, useValue: reportesApi },
        { provide: ClientesApi, useValue: clientesApi },
        { provide: ToastService, useValue: toast },
      ],
    });
    fixture = TestBed.createComponent(ReportesPage);
    await stable();
  });

  function fillFilter(): void {
    selectOption(query(fixture, '#reporte-cliente'), 'Marianela Montalvo');
    typeInto(query(fixture, '#reporte-inicio'), filtro.fechaInicio);
    typeInto(query(fixture, '#reporte-fin'), filtro.fechaFin);
  }

  it('muestra el mensaje inicial', () => {
    expect(textOf(fixture, 'app-empty-state')).toContain(
      'Seleccione un cliente y un rango de fechas.',
    );
    expect(reportesApi.generar).not.toHaveBeenCalled();
  });

  it('genera el reporte', async () => {
    reportesApi.generar.mockReturnValue(of(REPORTE));

    fillFilter();
    buttonByText(fixture, 'Generar reporte').click();
    await stable();

    expect(reportesApi.generar).toHaveBeenCalledWith(filtro);
    expect(textOf(fixture, 'app-reporte-view h2')).toBe('Marianela Montalvo');
  });

  it('atenúa el reporte anterior mientras carga el nuevo', async () => {
    const pending = new Subject<Reporte>();
    reportesApi.generar.mockReturnValueOnce(of(REPORTE)).mockReturnValueOnce(pending);
    fillFilter();
    buttonByText(fixture, 'Generar reporte').click();
    await stable();

    buttonByText(fixture, 'Generar reporte').click();
    await new Promise((resolve) => setTimeout(resolve));

    expect(query(fixture, 'app-reporte-view').classList).toContain('is-stale');
    expect(buttonByText(fixture, 'Generar reporte').disabled).toBe(true);

    pending.next({ ...REPORTE, cliente: 'Marianela Montalvo (actualizado)' });
    pending.complete();
    await stable();

    expect(query(fixture, 'app-reporte-view').classList).not.toContain('is-stale');
    expect(textOf(fixture, 'app-reporte-view h2')).toBe('Marianela Montalvo (actualizado)');
  });

  it('muestra el error del backend', async () => {
    reportesApi.generar.mockReturnValue(
      throwError(() => new ApiError(404, 'Cliente no encontrado')),
    );

    fillFilter();
    buttonByText(fixture, 'Generar reporte').click();
    await stable();

    expect(textOf(fixture, 'app-error-alert')).toBe('Cliente no encontrado');
    expect(queryAll(fixture, 'app-reporte-view')).toHaveLength(0);
  });

  describe('PDF', () => {
    const createObjectURL = jest.fn((blob: Blob) => `blob:${blob.type}`);
    let clickedLinks: HTMLAnchorElement[];

    beforeEach(() => {
      Object.assign(URL, { createObjectURL, revokeObjectURL: jest.fn() });
      clickedLinks = [];
      jest.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (
        this: HTMLAnchorElement,
      ) {
        clickedLinks.push(this);
      });
    });

    it('descarga el PDF con el nombre que manda el backend', async () => {
      reportesApi.descargarPdf.mockReturnValue(
        of({
          nombreArchivo: 'estado-cuenta-2-20220201-20220228.pdf',
          tipoContenido: 'application/pdf',
          contenidoBase64: btoa('%PDF-1.7'),
        }),
      );

      fillFilter();
      buttonByText(fixture, 'Descargar PDF').click();
      await stable();

      expect(reportesApi.descargarPdf).toHaveBeenCalledWith(filtro);
      const blob = createObjectURL.mock.calls[0][0];
      expect(blob.type).toBe('application/pdf');
      expect(await blob.text()).toBe('%PDF-1.7');
      expect(clickedLinks.map((link) => link.download)).toEqual([
        'estado-cuenta-2-20220201-20220228.pdf',
      ]);
      expect(toast.success).toHaveBeenCalledWith('PDF descargado');
    });

    it('muestra el error si falla el PDF', async () => {
      reportesApi.descargarPdf.mockReturnValue(
        throwError(() => new ApiError(500, 'Error en el servidor, intente más tarde')),
      );

      fillFilter();
      buttonByText(fixture, 'Descargar PDF').click();
      await stable();

      expect(toast.error).toHaveBeenCalledWith('Error en el servidor, intente más tarde');
      expect(clickedLinks).toEqual([]);
      expect(page().querySelector('button[disabled]')).toBeNull();
    });
  });
});
