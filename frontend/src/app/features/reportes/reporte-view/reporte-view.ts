import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, linkedSignal } from '@angular/core';

import {
  ESTADO_CUENTA,
  Reporte,
  ReporteMovimiento,
  TIPO_CUENTA_LABELS,
  estadoLabel,
} from '../../../core/models';
import { Icon } from '../../../shared/components/icon/icon';
import { QuickSearch } from '../../../shared/components/quick-search/quick-search';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { TablePanel } from '../../../shared/components/table-panel/table-panel';
import { CountUp } from '../../../shared/directives/count-up';
import { DateTimePipe } from '../../../shared/pipes/date-time-pipe';
import { MoneyPipe } from '../../../shared/pipes/money-pipe';
import { SearchableValue, filterBySearch } from '../../../shared/utils/text-search';
import { formatDateTime, formatMoney, formatSignedMoney } from '../../../shared/utils/formatters';
import { ReporteChart } from '../reporte-chart/reporte-chart';

@Component({
  selector: 'app-reporte-view',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CountUp,
    DatePipe,
    DateTimePipe,
    Icon,
    MoneyPipe,
    QuickSearch,
    ReporteChart,
    StatusBadge,
    TablePanel,
  ],
  templateUrl: './reporte-view.html',
  styleUrl: './reporte-view.scss',
})
export class ReporteView {
  readonly reporte = input.required<Reporte>();

  protected readonly tipoCuentaLabels = TIPO_CUENTA_LABELS;
  protected readonly estadoCuenta = ESTADO_CUENTA;
  protected readonly money = formatMoney;
  protected readonly signedMoney = formatSignedMoney;

  protected readonly saldoTotal = computed(() =>
    this.reporte().cuentas.reduce((total, cuenta) => total + cuenta.saldoDisponible, 0),
  );

  protected readonly query = linkedSignal({ source: this.reporte, computation: () => '' });

  protected readonly movimientosVisibles = computed(() =>
    filterBySearch(this.reporte().movimientos, this.query(), searchableValues),
  );
}

function searchableValues(movimiento: ReporteMovimiento): SearchableValue[] {
  return [
    formatDateTime(movimiento.fecha),
    movimiento.cliente,
    movimiento.numeroCuenta,
    TIPO_CUENTA_LABELS[movimiento.tipoCuenta],
    estadoLabel(movimiento.estado, ESTADO_CUENTA),
    formatMoney(movimiento.saldoInicial),
    movimiento.saldoInicial,
    formatSignedMoney(movimiento.movimiento),
    Math.abs(movimiento.movimiento),
    formatMoney(movimiento.saldoDisponible),
    movimiento.saldoDisponible,
  ];
}
