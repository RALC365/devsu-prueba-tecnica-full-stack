import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  buttonByText,
  query,
  queryAll,
  selectOption,
  textOf,
  typeInto,
} from '../../../../testing/dom';
import { CLIENTES } from '../../../../testing/fixtures';
import { ReporteFiltro } from '../../../core/models';
import { ReporteFilterForm } from './reporte-filter-form';

describe('ReporteFilterForm', () => {
  let fixture: ComponentFixture<ReporteFilterForm>;
  let generated: ReporteFiltro[];
  let downloaded: ReporteFiltro[];

  const stable = () => fixture.whenStable();
  const input = (id: string) => query<HTMLInputElement>(fixture, id);

  beforeEach(async () => {
    fixture = TestBed.createComponent(ReporteFilterForm);
    fixture.componentRef.setInput('clientes', { items: CLIENTES, loading: false, error: null });
    generated = [];
    downloaded = [];
    fixture.componentInstance.generate.subscribe((filtro) => generated.push(filtro));
    fixture.componentInstance.downloadPdf.subscribe((filtro) => downloaded.push(filtro));
    await stable();
  });

  it('por defecto va del inicio del mes a hoy', () => {
    const today = new Date();
    const pad = (value: number) => String(value).padStart(2, '0');
    const month = `${today.getFullYear()}-${pad(today.getMonth() + 1)}`;

    expect(input('#reporte-inicio').value).toBe(`${month}-01`);
    expect(input('#reporte-fin').value).toBe(`${month}-${pad(today.getDate())}`);
  });

  it('exige un cliente', async () => {
    buttonByText(fixture, 'Generar reporte').click();
    buttonByText(fixture, 'Descargar PDF').click();
    await stable();

    expect(textOf(fixture, '#reporte-cliente-error')).toBe('Campo obligatorio');
    expect(generated).toEqual([]);
    expect(downloaded).toEqual([]);
  });

  it('valida el rango de fechas', async () => {
    selectOption(query(fixture, '#reporte-cliente'), 'José Lema');
    typeInto(input('#reporte-inicio'), '2022-03-01');
    typeInto(input('#reporte-fin'), '2022-02-28');
    buttonByText(fixture, 'Generar reporte').click();
    await stable();

    expect(textOf(fixture, '#reporte-rango-error')).toBe(
      'La fecha de inicio no puede ser mayor a la fecha fin',
    );
    expect(input('#reporte-inicio').classList).toContain('is-invalid');
    expect(generated).toEqual([]);
  });

  it('emite el filtro al generar y al descargar', async () => {
    selectOption(query(fixture, '#reporte-cliente'), 'Marianela Montalvo');
    typeInto(input('#reporte-inicio'), '2022-02-01');
    typeInto(input('#reporte-fin'), '2022-02-28');

    buttonByText(fixture, 'Generar reporte').click();
    buttonByText(fixture, 'Descargar PDF').click();

    const filtro = { clienteId: 2, fechaInicio: '2022-02-01', fechaFin: '2022-02-28' };
    expect(generated).toEqual([filtro]);
    expect(downloaded).toEqual([filtro]);
  });

  it('deshabilita los botones mientras cargan', async () => {
    fixture.componentRef.setInput('generating', true);
    fixture.componentRef.setInput('downloading', true);
    await stable();

    expect(buttonByText(fixture, 'Generar reporte').disabled).toBe(true);
    expect(buttonByText(fixture, 'Descargar PDF').disabled).toBe(true);
    expect(queryAll(fixture, '.spinner')).toHaveLength(2);
  });
});
