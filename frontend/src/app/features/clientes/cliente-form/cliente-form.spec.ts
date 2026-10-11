import { ComponentFixture, TestBed } from '@angular/core/testing';

import {
  buttonByText,
  query,
  queryAll,
  selectOption,
  textOf,
  typeInto,
} from '../../../../testing/dom';
import { CLIENTES } from '../../../../testing/fixtures';
import { ApiError } from '../../../core/http/api-error';
import { Cliente, ClienteRequest } from '../../../core/models';
import { ClienteForm } from './cliente-form';

describe('ClienteForm', () => {
  let fixture: ComponentFixture<ClienteForm>;
  let submitted: ClienteRequest[];

  const stable = () => fixture.whenStable();
  const errorOf = (field: string) => textOf(fixture, `#cliente-${field}-error`);

  async function render(cliente: Cliente | null = null): Promise<void> {
    fixture = TestBed.createComponent(ClienteForm);
    fixture.componentRef.setInput('cliente', cliente);
    submitted = [];
    fixture.componentInstance.submitted.subscribe((request) => submitted.push(request));
    await stable();
  }

  function fillValidForm(): void {
    typeInto(query(fixture, '#cliente-nombre'), 'Ana Pérez');
    typeInto(query(fixture, '#cliente-identificacion'), 'AB-12345');
    selectOption(query(fixture, '#cliente-genero'), 'Otro');
    typeInto(query(fixture, '#cliente-edad'), '30');
    typeInto(query(fixture, '#cliente-telefono'), '+593991234567');
    typeInto(query(fixture, '#cliente-direccion'), 'Av. Amazonas N24');
    typeInto(query(fixture, '#cliente-contrasena'), 'secreta');
  }

  async function submit(): Promise<void> {
    query<HTMLFormElement>(fixture, 'form').dispatchEvent(new Event('submit'));
    await stable();
  }

  it('valida los campos obligatorios', async () => {
    await render();

    await submit();

    for (const field of [
      'nombre',
      'identificacion',
      'genero',
      'edad',
      'telefono',
      'direccion',
      'contrasena',
    ]) {
      expect(errorOf(field)).toBe('Campo obligatorio');
    }
    expect(submitted).toEqual([]);
  });

  it('valida los formatos', async () => {
    await render();

    typeInto(query(fixture, '#cliente-identificacion'), '12');
    typeInto(query(fixture, '#cliente-telefono'), '09-12');
    typeInto(query(fixture, '#cliente-edad'), '150');
    typeInto(query(fixture, '#cliente-contrasena'), '123');
    await stable();

    expect(errorOf('identificacion')).toBe('Entre 5 y 20 caracteres (letras, números o guiones)');
    expect(errorOf('telefono')).toBe('Entre 7 y 15 dígitos');
    expect(errorOf('edad')).toBe('Valor máximo: 120');
    expect(errorOf('contrasena')).toBe('Mínimo 4 caracteres');

    typeInto(query(fixture, '#cliente-edad'), '30.5');
    await stable();
    expect(errorOf('edad')).toBe('Debe ser un número entero');
  });

  it('emite el cliente nuevo con contraseña', async () => {
    await render();

    fillValidForm();
    await submit();

    expect(submitted).toEqual([
      {
        nombre: 'Ana Pérez',
        genero: 'OTRO',
        edad: 30,
        identificacion: 'AB-12345',
        direccion: 'Av. Amazonas N24',
        telefono: '+593991234567',
        contrasena: 'secreta',
        estado: true,
      },
    ]);
  });

  it('al editar la contraseña es opcional', async () => {
    await render(CLIENTES[2]);

    expect(textOf(fixture, '#cliente-contrasena-hint')).toBe('Dejar en blanco para no cambiarla');
    expect(query<HTMLInputElement>(fixture, '#cliente-nombre').value).toBe('Juan Osorio');
    expect(buttonByText(fixture, 'Guardar')).toBeTruthy();

    query<HTMLInputElement>(fixture, 'input[type="checkbox"]').click();
    await submit();

    expect(submitted).toEqual([
      {
        nombre: 'Juan Osorio',
        genero: 'MASCULINO',
        edad: 41,
        identificacion: '1734567890',
        direccion: '13 junio y Equinoccial',
        telefono: '098874587',
        estado: true,
      },
    ]);
  });

  it('envía la contraseña nueva al editar', async () => {
    await render(CLIENTES[0]);

    typeInto(query(fixture, '#cliente-contrasena'), 'nueva-clave');
    await submit();

    expect(submitted[0].contrasena).toBe('nueva-clave');
  });

  it('rechaza una contraseña con solo espacios', async () => {
    await render();
    fillValidForm();
    typeInto(query(fixture, '#cliente-contrasena'), '      ');
    await submit();

    expect(errorOf('contrasena')).toBe('No puede contener solo espacios');
    expect(submitted).toEqual([]);

    await render(CLIENTES[0]);
    typeInto(query(fixture, '#cliente-contrasena'), '   ');
    await submit();

    expect(errorOf('contrasena')).toBe('No puede contener solo espacios');
    expect(submitted).toEqual([]);
  });

  it('rechaza una contraseña de más de 72 bytes', async () => {
    await render(CLIENTES[0]);

    typeInto(query(fixture, '#cliente-contrasena'), 'ñ'.repeat(40));
    await submit();

    expect(errorOf('contrasena')).toBe('Máximo 72 bytes');
    expect(submitted).toEqual([]);
  });

  it('muestra el error del backend', async () => {
    await render();
    fillValidForm();

    fixture.componentRef.setInput(
      'serverError',
      new ApiError(400, 'La solicitud contiene datos inválidos', 'VALIDACION', {
        telefono: 'El teléfono ya está registrado',
        referencia: 'Referencia inválida',
      }),
    );
    await stable();

    expect(textOf(fixture, '.error-alert__message')).toBe('La solicitud contiene datos inválidos');
    expect(textOf(fixture, '.error-alert__details')).toBe('Referencia inválida');
    expect(errorOf('telefono')).toBe('El teléfono ya está registrado');

    await submit();
    expect(submitted).toEqual([]);
  });

  it('deshabilita Guardar mientras guarda y emite cancelled', async () => {
    await render();
    let cancelled = false;
    fixture.componentInstance.cancelled.subscribe(() => (cancelled = true));

    fixture.componentRef.setInput('saving', true);
    await stable();
    buttonByText(fixture, 'Cancelar').click();

    expect(buttonByText(fixture, 'Guardar').disabled).toBe(true);
    expect(queryAll(fixture, 'button[type="submit"] .spinner')).toHaveLength(1);
    expect(cancelled).toBe(true);
  });
});
