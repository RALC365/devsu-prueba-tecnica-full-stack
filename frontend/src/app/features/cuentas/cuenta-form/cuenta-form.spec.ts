import { ComponentFixture, TestBed } from '@angular/core/testing';

import { query, queryAll, selectOption, textOf, typeInto } from '../../../../testing/dom';
import { CLIENTES, CUENTAS } from '../../../../testing/fixtures';
import { Cliente, Cuenta, CuentaRequest } from '../../../core/models';
import { Lookup } from '../../../shared/data/lookup';
import { CuentaForm } from './cuenta-form';

describe('CuentaForm', () => {
  let fixture: ComponentFixture<CuentaForm>;
  let submitted: CuentaRequest[];

  const stable = () => fixture.whenStable();
  const loaded: Lookup<Cliente> = { items: CLIENTES, loading: false, error: null };

  async function render(
    cuenta: Cuenta | null = null,
    clientes: Lookup<Cliente> = loaded,
  ): Promise<void> {
    fixture = TestBed.createComponent(CuentaForm);
    fixture.componentRef.setInput('cuenta', cuenta);
    fixture.componentRef.setInput('clientes', clientes);
    submitted = [];
    fixture.componentInstance.submitted.subscribe((request) => submitted.push(request));
    await stable();
  }

  async function submit(): Promise<void> {
    query<HTMLFormElement>(fixture, 'form').dispatchEvent(new Event('submit'));
    await stable();
  }

  const clienteOptions = () =>
    queryAll<HTMLOptionElement>(fixture, '#cuenta-cliente option').map((option) => ({
      label: option.textContent?.trim(),
      disabled: option.disabled,
    }));

  it('lista los clientes y deshabilita los inactivos', async () => {
    await render();

    expect(clienteOptions()).toEqual([
      { label: 'Seleccione un cliente', disabled: true },
      { label: 'José Lema - 1712345678', disabled: false },
      { label: 'Marianela Montalvo - 1723456789', disabled: false },
      { label: 'Juan Osorio - 1734567890 (inactivo)', disabled: true },
    ]);
  });

  it('permite el titular inactivo al editar su cuenta', async () => {
    await render(CUENTAS[2]);

    expect(clienteOptions()[3]).toEqual({
      label: 'Juan Osorio - 1734567890 (inactivo)',
      disabled: false,
    });
    expect(query<HTMLInputElement>(fixture, '#cuenta-numero').value).toBe('495878');
  });

  it('muestra el aviso del titular solo al editar', async () => {
    await render();
    expect(queryAll(fixture, '#cuenta-cliente-hint')).toHaveLength(0);

    await render(CUENTAS[0]);
    expect(textOf(fixture, '#cuenta-cliente-hint')).toBe(
      'Solo se puede cambiar si la cuenta no tiene movimientos',
    );
  });

  it('muestra la carga y el error de clientes', async () => {
    await render(null, { items: [], loading: true, error: null });
    expect(clienteOptions()[0].label).toBe('Cargando clientes');

    fixture.componentRef.setInput('clientes', { items: [], loading: false, error: 'Sin conexión' });
    await stable();
    expect(textOf(fixture, '.form-field .form-error')).toBe(
      'No se pudieron cargar los clientes. Sin conexión',
    );
  });

  it('valida número de cuenta y saldo inicial', async () => {
    await render();

    await submit();
    expect(textOf(fixture, '#cuenta-numero-error')).toBe('Campo obligatorio');
    expect(textOf(fixture, '#cuenta-cliente-error')).toBe('Campo obligatorio');

    typeInto(query(fixture, '#cuenta-numero'), '12AB');
    typeInto(query(fixture, '#cuenta-saldo'), '-5');
    await stable();
    expect(textOf(fixture, '#cuenta-numero-error')).toBe('Entre 6 y 20 dígitos');
    expect(textOf(fixture, '#cuenta-saldo-error')).toBe('No puede ser negativo');

    typeInto(query(fixture, '#cuenta-saldo'), '10.555');
    await stable();
    expect(textOf(fixture, '#cuenta-saldo-error')).toBe('Máximo 2 decimales');
    expect(submitted).toEqual([]);
  });

  it('emite la cuenta editada', async () => {
    await render(CUENTAS[0]);

    selectOption(query(fixture, '#cuenta-tipo'), 'Corriente');
    typeInto(query(fixture, '#cuenta-saldo'), '2500.5');
    await submit();

    expect(submitted).toEqual([
      {
        numeroCuenta: '478758',
        tipoCuenta: 'CORRIENTE',
        saldoInicial: 2500.5,
        estado: true,
        clienteId: 1,
      },
    ]);
  });
});
