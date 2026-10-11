import { ComponentFixture, TestBed } from '@angular/core/testing';

import { query, queryAll, selectOption, textOf, typeInto } from '../../../../testing/dom';
import { CUENTAS, MOVIMIENTOS } from '../../../../testing/fixtures';
import { ApiError } from '../../../core/http/api-error';
import { Cuenta, Movimiento, MovimientoRequest } from '../../../core/models';
import { Lookup } from '../../../shared/data/lookup';
import { MovimientoForm } from './movimiento-form';

describe('MovimientoForm', () => {
  let fixture: ComponentFixture<MovimientoForm>;
  let submitted: MovimientoRequest[];

  const stable = () => fixture.whenStable();
  const cuentas: Lookup<Cuenta> = { items: CUENTAS, loading: false, error: null };

  async function render(movimiento: Movimiento | null = null): Promise<void> {
    fixture = TestBed.createComponent(MovimientoForm);
    fixture.componentRef.setInput('movimiento', movimiento);
    fixture.componentRef.setInput('cuentas', cuentas);
    submitted = [];
    fixture.componentInstance.submitted.subscribe((request) => submitted.push(request));
    await stable();
  }

  async function submit(): Promise<void> {
    query<HTMLFormElement>(fixture, 'form').dispatchEvent(new Event('submit'));
    await stable();
  }

  it('lista las cuentas y deshabilita las inactivas', async () => {
    await render();

    const options = queryAll<HTMLOptionElement>(fixture, '#movimiento-cuenta option');
    expect(options.map((option) => option.textContent?.replace(/\s+/g, ' ').trim())).toEqual([
      'Seleccione una cuenta',
      '478758 - Ahorros - José Lema',
      '225487 - Corriente - Marianela Montalvo',
      '495878 - Ahorros - Juan Osorio (inactiva)',
    ]);
    expect(options[3].disabled).toBe(true);
  });

  it('muestra el saldo de la cuenta seleccionada', async () => {
    await render();

    selectOption(query(fixture, '#movimiento-cuenta'), '478758');
    await stable();

    expect(textOf(fixture, '#movimiento-cuenta-hint')).toBe('Saldo disponible$1,425.00');
  });

  it('valida el valor', async () => {
    await render();

    await submit();
    expect(textOf(fixture, '#movimiento-valor-error')).toBe('Campo obligatorio');

    typeInto(query(fixture, '#movimiento-valor'), '0');
    await stable();
    expect(textOf(fixture, '#movimiento-valor-error')).toBe('Debe ser mayor a 0');

    typeInto(query(fixture, '#movimiento-valor'), '10.123');
    await stable();
    expect(textOf(fixture, '#movimiento-valor-error')).toBe('Máximo 2 decimales');
    expect(submitted).toEqual([]);
  });

  it('emite un depósito', async () => {
    await render();

    selectOption(query(fixture, '#movimiento-cuenta'), '225487');
    selectOption(query(fixture, '#movimiento-tipo'), 'Depósito');
    typeInto(query(fixture, '#movimiento-valor'), '600');
    await submit();

    expect(submitted).toEqual([{ numeroCuenta: '225487', tipoMovimiento: 'CREDITO', valor: 600 }]);
  });

  it('al editar bloquea la cuenta y usa el valor sin signo', async () => {
    await render(MOVIMIENTOS[1]);

    expect(query<HTMLSelectElement>(fixture, '#movimiento-cuenta').disabled).toBe(true);
    expect(query<HTMLInputElement>(fixture, '#movimiento-valor').value).toBe('575');

    selectOption(query(fixture, '#movimiento-tipo'), 'Depósito');
    await submit();

    expect(submitted).toEqual([{ numeroCuenta: '478758', tipoMovimiento: 'CREDITO', valor: 575 }]);
  });

  it('muestra el error del backend en el campo valor', async () => {
    await render();

    fixture.componentRef.setInput(
      'serverError',
      new ApiError(400, 'La solicitud contiene datos inválidos', 'VALIDACION', {
        valor: 'El valor debe ser mayor a 0',
      }),
    );
    await stable();

    expect(textOf(fixture, '#movimiento-valor-error')).toBe('El valor debe ser mayor a 0');
    expect(textOf(fixture, '.error-alert__message')).toBe('La solicitud contiene datos inválidos');
  });
});
