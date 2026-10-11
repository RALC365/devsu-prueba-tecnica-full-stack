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
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ApiError } from '../../../core/http/api-error';
import {
  Cliente,
  Cuenta,
  CuentaRequest,
  TIPOS_CUENTA,
  TIPO_CUENTA_LABELS,
  TipoCuenta,
  clienteLabel,
} from '../../../core/models';
import { ErrorAlert } from '../../../shared/components/error-alert/error-alert';
import { FieldError } from '../../../shared/components/field-error/field-error';
import { Icon } from '../../../shared/components/icon/icon';
import { Spinner } from '../../../shared/components/spinner/spinner';
import { Lookup } from '../../../shared/data/lookup';
import { SelectOption } from '../../../shared/forms/select-option';
import { applyFieldErrors, unmatchedFieldErrors } from '../../../shared/forms/server-errors';
import { ValidationMessages } from '../../../shared/forms/validation-messages';
import { maxDecimals } from '../../../shared/forms/validators';

const NUMERO_CUENTA_PATTERN = /^[0-9]{6,20}$/;

@Component({
  selector: 'app-cuenta-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, ErrorAlert, FieldError, Icon, Spinner],
  templateUrl: './cuenta-form.html',
})
export class CuentaForm implements OnInit {
  readonly cuenta = input<Cuenta | null>(null);
  readonly clientes = input.required<Lookup<Cliente>>();
  readonly saving = input(false);
  readonly serverError = input<ApiError | null>(null);
  readonly submitted = output<CuentaRequest>();
  readonly cancelled = output();

  protected readonly tiposCuenta = TIPOS_CUENTA;
  protected readonly tipoCuentaLabels = TIPO_CUENTA_LABELS;
  protected readonly isEdit = computed(() => this.cuenta() !== null);
  protected readonly messages = {
    numeroCuenta: { pattern: 'Entre 6 y 20 dígitos' },
    saldoInicial: { min: 'No puede ser negativo' },
  } satisfies Record<string, ValidationMessages>;

  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    numeroCuenta: ['', [Validators.required, Validators.pattern(NUMERO_CUENTA_PATTERN)]],
    tipoCuenta: this.fb.control<TipoCuenta | null>(null, Validators.required),
    saldoInicial: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(0),
      maxDecimals(2),
    ]),
    estado: [true],
    clienteId: this.fb.control<number | null>(null, Validators.required),
  });

  protected readonly clienteOptions = computed<SelectOption<number>[]>(() =>
    this.clientes().items.map((cliente) => ({
      value: cliente.clienteId,
      label: cliente.estado ? clienteLabel(cliente) : `${clienteLabel(cliente)} (inactivo)`,
      disabled: !cliente.estado && cliente.clienteId !== this.cuenta()?.clienteId,
    })),
  );

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
    const cuenta = this.cuenta();
    if (cuenta) {
      this.form.patchValue(cuenta);
    }
  }

  protected submit(): void {
    const { tipoCuenta, saldoInicial, clienteId, ...datos } = this.form.getRawValue();
    if (this.form.invalid || tipoCuenta === null || saldoInicial === null || clienteId === null) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.emit({ ...datos, tipoCuenta, saldoInicial, clienteId });
  }
}
