import { ChangeDetectionStrategy, Component, computed, inject, input, output } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { map } from 'rxjs';

import { Cliente, ReporteFiltro, clienteLabel } from '../../../core/models';
import { FieldError } from '../../../shared/components/field-error/field-error';
import { Icon } from '../../../shared/components/icon/icon';
import { Spinner } from '../../../shared/components/spinner/spinner';
import { Lookup } from '../../../shared/data/lookup';
import { SelectOption } from '../../../shared/forms/select-option';
import { dateRange } from '../../../shared/forms/validators';
import { toIsoDate } from '../../../shared/utils/formatters';

export interface Periodo {
  readonly id: string;
  readonly label: string;
  readonly rango: (hoy: Date) => readonly [Date, Date];
}

export const PERIODOS: readonly Periodo[] = [
  {
    id: 'mes',
    label: 'Este mes',
    rango: (hoy) => [new Date(hoy.getFullYear(), hoy.getMonth(), 1), hoy],
  },
  {
    id: 'mes-anterior',
    label: 'Mes anterior',
    rango: (hoy) => [
      new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1),
      new Date(hoy.getFullYear(), hoy.getMonth(), 0),
    ],
  },
  {
    id: 'ultimos-30',
    label: 'Últimos 30 días',
    rango: (hoy) => [new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - 29), hoy],
  },
  {
    id: 'anio',
    label: 'Este año',
    rango: (hoy) => [new Date(hoy.getFullYear(), 0, 1), hoy],
  },
];

@Component({
  selector: 'app-reporte-filter-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, FieldError, Icon, Spinner],
  templateUrl: './reporte-filter-form.html',
  styleUrl: './reporte-filter-form.scss',
})
export class ReporteFilterForm {
  readonly clientes = input.required<Lookup<Cliente>>();
  readonly generating = input(false);
  readonly downloading = input(false);
  readonly generate = output<ReporteFiltro>();
  readonly downloadPdf = output<ReporteFiltro>();

  protected readonly periodos = PERIODOS;
  protected readonly clienteOptions = computed<SelectOption<number>[]>(() =>
    this.clientes().items.map((cliente) => ({
      value: cliente.clienteId,
      label: clienteLabel(cliente),
    })),
  );

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly today = new Date();
  private readonly mesActual = PERIODOS[0].rango(this.today);

  protected readonly form = this.fb.group(
    {
      clienteId: this.fb.control<number | null>(null, Validators.required),
      fechaInicio: [toIsoDate(this.mesActual[0]), Validators.required],
      fechaFin: [toIsoDate(this.mesActual[1]), Validators.required],
    },
    { validators: dateRange('fechaInicio', 'fechaFin') },
  );

  private readonly fechas = toSignal(
    this.form.valueChanges.pipe(map(() => this.form.getRawValue())),
    { initialValue: this.form.getRawValue() },
  );

  // el atajo queda marcado mientras las fechas coincidan con él
  protected readonly periodoActivo = computed(() => {
    const { fechaInicio, fechaFin } = this.fechas();
    return (
      PERIODOS.find((periodo) => {
        const [inicio, fin] = periodo.rango(this.today);
        return toIsoDate(inicio) === fechaInicio && toIsoDate(fin) === fechaFin;
      })?.id ?? null
    );
  });

  protected aplicarPeriodo(periodo: Periodo): void {
    const [inicio, fin] = periodo.rango(this.today);
    this.form.patchValue({ fechaInicio: toIsoDate(inicio), fechaFin: toIsoDate(fin) });
  }

  protected onGenerate(): void {
    this.emitIfValid((filtro) => this.generate.emit(filtro));
  }

  protected onDownloadPdf(): void {
    this.emitIfValid((filtro) => this.downloadPdf.emit(filtro));
  }

  private emitIfValid(emit: (filtro: ReporteFiltro) => void): void {
    const { clienteId, fechaInicio, fechaFin } = this.form.getRawValue();
    if (this.form.invalid || clienteId === null) {
      this.form.markAllAsTouched();
      return;
    }
    emit({ clienteId, fechaInicio, fechaFin });
  }
}
