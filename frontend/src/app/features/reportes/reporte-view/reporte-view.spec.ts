import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  normalizeSpaces,
  query,
  queryAll,
  tableRows,
  textOf,
  typeInto,
} from '../../../../testing/dom';
import { REPORTE } from '../../../../testing/fixtures';
import { ReporteView } from './reporte-view';

describe('ReporteView', () => {
  let fixture: ComponentFixture<ReporteView>;

  const stable = () => fixture.whenStable();
  const texts = (selector: string) =>
    queryAll(fixture, selector).map((element) => normalizeSpaces(element.textContent ?? ''));
  const cuentas = () => tableRows(fixture, '[aria-labelledby="reporte-cuentas-titulo"] table');
  const movimientos = () =>
    tableRows(fixture, '[aria-labelledby="reporte-movimientos-titulo"] table');

  beforeEach(async () => {
    fixture = TestBed.createComponent(ReporteView);
    fixture.componentRef.setInput('reporte', REPORTE);
    await stable();
  });

  it('muestra cliente, periodo y fecha de generación', () => {
    expect(textOf(fixture, '.reporte-header h2')).toBe('Marianela Montalvo');
    expect(textOf(fixture, '.reporte-header__meta')).toBe('Identificación 1723456789');
    expect(texts('.reporte-header__details dd')).toEqual([
      '01/02/2022 al 28/02/2022',
      '10/10/2026 10:15',
    ]);
  });

  it('muestra el resumen', () => {
    expect(texts('.summary__label')).toEqual([
      'Total créditos',
      'Total débitos',
      'Saldo al corte',
      'Movimientos',
    ]);
    expect(texts('.summary__value')).toEqual(['+$600.00', '-$540.00', '$700.00', '2']);
    expect(query(fixture, '.summary__value--credit').textContent).toContain('$600.00');
    expect(query(fixture, '.summary__value--debit').textContent).toContain('$540.00');
  });

  it('lista las cuentas', () => {
    expect(texts('[aria-labelledby="reporte-cuentas-titulo"] th')).toEqual([
      'Número cuenta',
      'Tipo',
      'Saldo inicial',
      'Saldo al corte',
      'Estado',
      'Créditos',
      'Débitos',
    ]);
    expect(cuentas()).toEqual([
      ['225487', 'Corriente', '$100.00', '$700.00', 'Activa', '+$600.00', '$0.00'],
      ['496825', 'Ahorros', '$540.00', '$0.00', 'Activa', '$0.00', '-$540.00'],
    ]);
  });

  it('lista y filtra los movimientos', async () => {
    expect(movimientos()).toEqual([
      [
        '08/02/2022 10:00',
        'Marianela Montalvo',
        '496825',
        'Ahorros',
        '$540.00',
        'Activa',
        '-$540.00',
        '$0.00',
      ],
      [
        '10/02/2022 11:30',
        'Marianela Montalvo',
        '225487',
        'Corriente',
        '$100.00',
        'Activa',
        '+$600.00',
        '$700.00',
      ],
    ]);

    typeInto(query(fixture, 'input[type="search"]'), 'corriente');
    await stable();

    expect(movimientos().map((cells) => cells[2])).toEqual(['225487']);
    expect(textOf(fixture, '.quick-search__count')).toBe('1 de 2 registros');
  });

  it('limpia la búsqueda al llegar un reporte nuevo', async () => {
    typeInto(query(fixture, 'input[type="search"]'), 'corriente');
    await stable();

    fixture.componentRef.setInput('reporte', { ...REPORTE });
    await stable();

    expect(query<HTMLInputElement>(fixture, 'input[type="search"]').value).toBe('');
    expect(textOf(fixture, '.quick-search__count')).toBe('2 de 2 registros');
  });
});
