import { ValidationErrors } from '@angular/forms';

export type ValidationMessages = Readonly<Partial<Record<string, string>>>;

type MessageFactory = (error: unknown) => string;

const FALLBACK_MESSAGE = 'Valor inválido';

const DEFAULT_MESSAGES: Readonly<Record<string, MessageFactory>> = {
  required: () => 'Campo obligatorio',
  blank: () => 'No puede contener solo espacios',
  minlength: (error) => `Mínimo ${param(error, 'requiredLength')} caracteres`,
  maxlength: (error) => `Máximo ${param(error, 'requiredLength')} caracteres`,
  maxBytes: (error) => `Máximo ${param(error, 'max')} bytes`,
  min: (error) => `Valor mínimo: ${param(error, 'min')}`,
  max: (error) => `Valor máximo: ${param(error, 'max')}`,
  maxDecimals: (error) => `Máximo ${param(error, 'max')} decimales`,
  pattern: () => 'Formato inválido',
  dateRange: () => 'La fecha de inicio no puede ser mayor a la fecha fin',
  server: (error) => (typeof error === 'string' ? error : FALLBACK_MESSAGE),
};

export function getErrorMessage(
  errors: ValidationErrors | null,
  messages: ValidationMessages = {},
): string | null {
  const key = errors && 'server' in errors ? 'server' : Object.keys(errors ?? {})[0];
  if (!errors || key === undefined) {
    return null;
  }
  const error: unknown = errors[key];
  return messages[key] ?? DEFAULT_MESSAGES[key]?.(error) ?? FALLBACK_MESSAGE;
}

function param(error: unknown, name: string): unknown {
  return typeof error === 'object' && error !== null
    ? (error as Record<string, unknown>)[name]
    : '';
}
