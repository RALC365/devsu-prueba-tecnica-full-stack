import { ComponentFixture, TestBed } from '@angular/core/testing';

import { normalizeSpaces, queryAll, textOf } from '../../../../testing/dom';
import { REPORTE } from '../../../../testing/fixtures';
import { ReporteCuenta } from '../../../core/models';
import { ReporteChart } from './reporte-chart';

describe('ReporteChart', () => {
  let fixture: ComponentFixture<ReporteChart>;

  async function render(cuentas: readonly ReporteCuenta[]): Promise<void> {
    fixture = TestBed.createComponent(ReporteChart);
    fixture.componentRef.setInput('cuentas', cuentas);
    await fixture.whenStable();
  }

  const barLengths = () =>
    queryAll(fixture, '.chart__bar').map((bar) => bar.style.getPropertyValue('--bar-pct'));

  it('calcula el largo de las barras según el mayor monto', async () => {
    await render(REPORTE.cuentas);

    expect(barLengths()).toEqual(['0', '100', '90', '0']);
  });

  it('muestra la cuenta y el monto de cada barra', async () => {
    await render(REPORTE.cuentas);

    const texts = (selector: string) =>
      queryAll(fixture, selector).map((element) => normalizeSpaces(element.textContent ?? ''));
    expect(texts('.chart__account')).toEqual(['225487', '496825']);
    expect(texts('.chart__type')).toEqual(['Corriente', 'Ahorros']);
    expect(texts('.chart__value')).toEqual([
      'Débitos: $0.00',
      'Créditos: $600.00',
      'Débitos: $540.00',
      'Créditos: $0.00',
    ]);
  });

  it('sin movimientos las barras quedan en 0', async () => {
    await render([{ ...REPORTE.cuentas[0], totalCreditos: 0, totalDebitos: 0 }]);

    expect(barLengths()).toEqual(['0', '0']);
  });

  it('muestra un mensaje si no hay cuentas', async () => {
    await render([]);

    expect(textOf(fixture, '.chart-empty')).toBe('El cliente no tiene cuentas');
  });
});
