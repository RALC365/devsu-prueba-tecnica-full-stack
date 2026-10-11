import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { ReporteCuenta, TIPO_CUENTA_LABELS } from '../../../core/models';
import { MoneyPipe } from '../../../shared/pipes/money-pipe';

interface ChartRow {
  readonly numeroCuenta: string;
  readonly tipo: string;
  readonly creditos: number;
  readonly debitos: number;
  readonly neto: number;
  readonly creditosPct: number;
  readonly debitosPct: number;
}

@Component({
  selector: 'app-reporte-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MoneyPipe],
  templateUrl: './reporte-chart.html',
  styleUrl: './reporte-chart.scss',
})
export class ReporteChart {
  readonly cuentas = input.required<readonly ReporteCuenta[]>();

  protected readonly rows = computed<readonly ChartRow[]>(() => {
    const cuentas = this.cuentas();
    const max = Math.max(0, ...cuentas.flatMap((c) => [c.totalCreditos, Math.abs(c.totalDebitos)]));
    const percentage = (value: number) => (max > 0 ? (value / max) * 100 : 0);

    return cuentas.map((cuenta) => ({
      numeroCuenta: cuenta.numeroCuenta,
      tipo: TIPO_CUENTA_LABELS[cuenta.tipoCuenta],
      creditos: cuenta.totalCreditos,
      debitos: Math.abs(cuenta.totalDebitos),
      neto: cuenta.totalCreditos + cuenta.totalDebitos,
      creditosPct: percentage(cuenta.totalCreditos),
      debitosPct: percentage(Math.abs(cuenta.totalDebitos)),
    }));
  });
}
