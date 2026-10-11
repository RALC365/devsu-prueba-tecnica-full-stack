import { FormControl, FormGroup, Validators } from '@angular/forms';

import { applyFieldErrors, unmatchedFieldErrors } from './server-errors';

describe('errores del servidor', () => {
  const createForm = () =>
    new FormGroup({
      numeroCuenta: new FormControl('478758', Validators.required),
      valor: new FormControl(0),
    });

  it('asigna el error del backend al control', () => {
    const form = createForm();

    applyFieldErrors(form, { valor: 'El valor debe ser mayor a 0', otro: 'Ignorado' });

    expect(form.controls.valor.errors).toEqual({ server: 'El valor debe ser mayor a 0' });
    expect(form.controls.valor.touched).toBe(true);
    expect(form.controls.numeroCuenta.errors).toBeNull();
    expect(form.invalid).toBe(true);
  });

  it('el error se limpia al editar el campo', () => {
    const form = createForm();
    applyFieldErrors(form, { valor: 'El valor debe ser mayor a 0' });

    form.controls.valor.setValue(50);

    expect(form.controls.valor.errors).toBeNull();
  });

  it('devuelve los errores sin campo en el formulario', () => {
    expect(
      unmatchedFieldErrors(createForm(), { valor: 'Inválido', cuenta: 'Cuenta inactiva' }),
    ).toEqual(['Cuenta inactiva']);
  });
});
