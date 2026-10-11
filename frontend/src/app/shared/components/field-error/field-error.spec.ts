import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';

import { FieldError } from './field-error';

@Component({
  imports: [FieldError],
  template: `<app-field-error
    [control]="control"
    [messages]="{ pattern: 'Entre 7 y 15 dígitos' }"
  />`,
})
class Host {
  readonly control = new FormControl('', [Validators.required, Validators.pattern(/^\d{7,15}$/)]);
}

describe('FieldError', () => {
  let fixture: ComponentFixture<Host>;
  let control: FormControl<string | null>;

  const message = () => (fixture.nativeElement as HTMLElement).textContent?.trim() ?? '';

  beforeEach(async () => {
    fixture = TestBed.createComponent(Host);
    control = fixture.componentInstance.control;
    await fixture.whenStable();
  });

  it('no muestra nada hasta que se toca', async () => {
    expect(message()).toBe('');

    control.markAsTouched();
    await fixture.whenStable();

    expect(message()).toBe('Campo obligatorio');
  });

  it('usa los mensajes propios', async () => {
    control.markAsTouched();
    control.setValue('12ab');
    await fixture.whenStable();
    expect(message()).toBe('Entre 7 y 15 dígitos');

    control.setValue('0982547850');
    await fixture.whenStable();
    expect(message()).toBe('');
  });

  it('muestra el mensaje del backend', async () => {
    control.setValue('0982547850');
    control.setErrors({ server: 'El teléfono ya está registrado' });
    control.markAsTouched();
    await fixture.whenStable();

    expect(message()).toBe('El teléfono ya está registrado');
  });
});
