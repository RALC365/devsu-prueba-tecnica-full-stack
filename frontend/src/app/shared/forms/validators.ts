import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const utf8 = new TextEncoder();

export function maxDecimals(max: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value: unknown = control.value;
    if (typeof value !== 'number' || !Number.isFinite(value)) {
      return null;
    }
    const decimals = value.toString().split('.')[1]?.length ?? 0;
    return decimals > max ? { maxDecimals: { max } } : null;
  };
}

export function notBlank(control: AbstractControl): ValidationErrors | null {
  const value: unknown = control.value;
  return typeof value === 'string' && value !== '' && value.trim() === '' ? { blank: true } : null;
}

export function maxBytes(max: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value: unknown = control.value;
    if (typeof value !== 'string') {
      return null;
    }
    const bytes = utf8.encode(value).length;
    return bytes > max ? { maxBytes: { max, actual: bytes } } : null;
  };
}

export function dateRange(startControl: string, endControl: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const start: unknown = group.get(startControl)?.value;
    const end: unknown = group.get(endControl)?.value;
    const isInvalid =
      typeof start === 'string' &&
      typeof end === 'string' &&
      start !== '' &&
      end !== '' &&
      start > end;
    return isInvalid ? { dateRange: true } : null;
  };
}
