import { FormGroup } from '@angular/forms';

import { FieldErrors } from '../../core/http/api-error';

export function applyFieldErrors(form: FormGroup, fieldErrors: FieldErrors): void {
  for (const [field, message] of Object.entries(fieldErrors)) {
    const control = form.get(field);
    control?.setErrors({ ...control.errors, server: message });
    control?.markAsTouched();
  }
}

export function unmatchedFieldErrors(form: FormGroup, fieldErrors: FieldErrors): string[] {
  return Object.entries(fieldErrors)
    .filter(([field]) => form.get(field) === null)
    .map(([, message]) => message);
}
