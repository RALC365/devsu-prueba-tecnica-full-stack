import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  effect,
  inject,
  input,
  output,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ApiError } from '../../../core/http/api-error';
import {
  Cuenta,
  Movimiento,
  MovimientoRequest,
  TIPOS_MOVIMIENTO,
  TipoMovimiento,
  cuentaLabel,
} from '../../../core/models';
import { ErrorAlert } from '../../../shared/components/error-alert/error-alert';
import { FieldError } from '../../../shared/components/field-error/field-error';
import { Icon, IconName } from '../../../shared/components/icon/icon';
import { Spinner } from '../../../shared/components/spinner/spinner';
import { Lookup } from '../../../shared/data/lookup';
import { SelectOption } from '../../../shared/forms/select-option';
import { applyFieldErrors, unmatchedFieldErrors } from '../../../shared/forms/server-errors';
import { ValidationMessages } from '../../../shared/forms/validation-messages';
import { maxDecimals } from '../../../shared/forms/validators';
import { MoneyPipe } from '../../../shared/pipes/money-pipe';

interface Operacion {
  readonly label: string;
  readonly hint: string;
  readonly icon: IconName;
}

const OPERACIONES: Readonly<Record<TipoMovimiento, Operacion>> = {
  CREDITO: { label: 'Depósito', hint: 'Crédito', icon: 'arrow-down-left' },
  DEBITO: { label: 'Retiro', hint: 'Débito', icon: 'arrow-up-right' },
};

export interface SaldoPreview {
  readonly actual: number;
  readonly cambio: number | null;
  readonly despues: number | null;
}

@Component({
  selector: 'app-movimiento-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, ErrorAlert, FieldError, Icon, MoneyPipe, Spinner],
  templateUrl: './movimiento-form.html',
  styleUrl: './movimiento-form.scss',
})
export class MovimientoForm implements OnInit {
  readonly movimiento = input<Movimiento | null>(null);
  readonly cuentas = input.required<Lookup<Cuenta>>();
  readonly saving = input(false);
  readonly serverError = input<ApiError | null>(null);
  readonly submitted = output<MovimientoRequest>();
  readonly cancelled = output();

  protected readonly tiposMovimiento = TIPOS_MOVIMIENTO;
  protected readonly operaciones = OPERACIONES;
  protected readonly messages = {
    valor: { min: 'Debe ser mayor a 0' },
  } satisfies Record<string, ValidationMessages>;

  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    numeroCuenta: ['', Validators.required],
    tipoMovimiento: this.fb.control<TipoMovimiento | null>(null, Validators.required),
    valor: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(0.01),
      maxDecimals(2),
    ]),
  });

  private readonly numeroCuenta = toSignal(this.form.controls.numeroCuenta.valueChanges, {
    initialValue: this.form.controls.numeroCuenta.value,
  });
  private readonly tipo = toSignal(this.form.controls.tipoMovimiento.valueChanges, {
    initialValue: this.form.controls.tipoMovimiento.value,
  });
  private readonly valor = toSignal(this.form.controls.valor.valueChanges, {
    initialValue: this.form.controls.valor.value,
  });

  protected readonly cuentaOptions = computed<SelectOption<string>[]>(() =>
    this.cuentas().items.map((cuenta) => ({
      value: cuenta.numeroCuenta,
      label: cuenta.estado ? cuentaLabel(cuenta) : `${cuentaLabel(cuenta)} (inactiva)`,
      disabled: !cuenta.estado && cuenta.numeroCuenta !== this.movimiento()?.numeroCuenta,
    })),
  );

  protected readonly cuentaSeleccionada = computed(
    () =>
      this.cuentas().items.find((cuenta) => cuenta.numeroCuenta === this.numeroCuenta()) ?? null,
  );

  // Saldo que quedaría al guardar. Al editar se descuenta el valor original,
  // que ya está incluido en el saldo disponible.
  protected readonly preview = computed<SaldoPreview | null>(() => {
    const cuenta = this.cuentaSeleccionada();
    if (!cuenta) {
      return null;
    }
    const tipo = this.tipo();
    const valor = this.valor();
    if (tipo === null || typeof valor !== 'number' || !(valor > 0)) {
      return { actual: cuenta.saldoDisponible, cambio: null, despues: null };
    }
    const original = this.movimiento();
    const anterior = original?.numeroCuenta === cuenta.numeroCuenta ? original.valor : 0;
    const cambio = roundCents((tipo === 'CREDITO' ? valor : -valor) - anterior);
    return {
      actual: cuenta.saldoDisponible,
      cambio,
      despues: roundCents(cuenta.saldoDisponible + cambio),
    };
  });

  protected readonly cambioLabel = computed(() => {
    if (this.movimiento()) {
      return 'Diferencia';
    }
    return this.tipo() === 'DEBITO' ? 'Retiro' : 'Depósito';
  });

  protected readonly unmatchedServerErrors = computed(() =>
    unmatchedFieldErrors(this.form, this.serverError()?.fieldErrors ?? {}),
  );

  constructor() {
    effect(() => {
      const error = this.serverError();
      if (error) {
        applyFieldErrors(this.form, error.fieldErrors);
      }
    });
  }

  ngOnInit(): void {
    const movimiento = this.movimiento();
    if (movimiento) {
      const { numeroCuenta, tipoMovimiento, valor } = movimiento;
      this.form.patchValue({ numeroCuenta, tipoMovimiento, valor: Math.abs(valor) });
      this.form.controls.numeroCuenta.disable();
    }
  }

  protected submit(): void {
    const { numeroCuenta, tipoMovimiento, valor } = this.form.getRawValue();
    if (this.form.invalid || tipoMovimiento === null || valor === null) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.emit({ numeroCuenta, tipoMovimiento, valor });
  }
}

function roundCents(value: number): number {
  return Math.round(value * 100) / 100;
}
