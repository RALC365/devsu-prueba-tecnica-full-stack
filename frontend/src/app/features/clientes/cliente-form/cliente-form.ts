import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { ApiError } from '../../../core/http/api-error';
import { Cliente, ClienteRequest, GENEROS, GENERO_LABELS, Genero } from '../../../core/models';
import { ErrorAlert } from '../../../shared/components/error-alert/error-alert';
import { FieldError } from '../../../shared/components/field-error/field-error';
import { Icon } from '../../../shared/components/icon/icon';
import { Spinner } from '../../../shared/components/spinner/spinner';
import { applyFieldErrors, unmatchedFieldErrors } from '../../../shared/forms/server-errors';
import { ValidationMessages } from '../../../shared/forms/validation-messages';
import { maxBytes, notBlank } from '../../../shared/forms/validators';

const IDENTIFICACION_PATTERN = /^[0-9A-Za-z-]{5,20}$/;
const TELEFONO_PATTERN = /^\+?[0-9]{7,15}$/;
const EDAD_ENTERA_PATTERN = /^\d+$/;
// BCrypt solo usa los primeros 72 bytes
const CONTRASENA_MAX_BYTES = 72;

@Component({
  selector: 'app-cliente-form',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, ErrorAlert, FieldError, Icon, Spinner],
  templateUrl: './cliente-form.html',
})
export class ClienteForm implements OnInit {
  readonly cliente = input<Cliente | null>(null);
  readonly saving = input(false);
  readonly serverError = input<ApiError | null>(null);
  readonly submitted = output<ClienteRequest>();
  readonly cancelled = output();

  protected readonly generos = GENEROS;
  protected readonly generoLabels = GENERO_LABELS;
  protected readonly isEdit = computed(() => this.cliente() !== null);
  protected readonly showPassword = signal(false);
  protected readonly messages = {
    edad: { pattern: 'Debe ser un número entero' },
    identificacion: { pattern: 'Entre 5 y 20 caracteres (letras, números o guiones)' },
    telefono: { pattern: 'Entre 7 y 15 dígitos' },
  } satisfies Record<string, ValidationMessages>;

  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    genero: this.fb.control<Genero | null>(null, Validators.required),
    edad: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(120),
      Validators.pattern(EDAD_ENTERA_PATTERN),
    ]),
    identificacion: ['', [Validators.required, Validators.pattern(IDENTIFICACION_PATTERN)]],
    direccion: ['', [Validators.required, Validators.maxLength(200)]],
    telefono: ['', [Validators.required, Validators.pattern(TELEFONO_PATTERN)]],
    contrasena: [
      '',
      [
        Validators.required,
        notBlank,
        Validators.minLength(4),
        Validators.maxLength(50),
        maxBytes(CONTRASENA_MAX_BYTES),
      ],
    ],
    estado: [true],
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
    const cliente = this.cliente();
    if (cliente) {
      this.form.patchValue(cliente);
      this.form.controls.contrasena.removeValidators(Validators.required);
      this.form.controls.contrasena.updateValueAndValidity();
    }
  }

  protected submit(): void {
    const { genero, edad, contrasena, ...datos } = this.form.getRawValue();
    if (this.form.invalid || genero === null || edad === null) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.emit({ ...datos, genero, edad, ...(contrasena ? { contrasena } : {}) });
  }
}
