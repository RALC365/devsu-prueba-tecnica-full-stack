import { ComponentFixture, TestBed } from '@angular/core/testing';

import { queryAll } from '../../../../testing/dom';
import { ToastService } from '../../../core/notifications/toast-service';
import { ToastContainer } from './toast-container';

describe('ToastContainer', () => {
  let fixture: ComponentFixture<ToastContainer>;

  const messages = () => queryAll(fixture, '.toast').map((toast) => toast.textContent?.trim());

  beforeEach(async () => {
    fixture = TestBed.createComponent(ToastContainer);
    const toasts = TestBed.inject(ToastService);
    toasts.success('Cliente creado');
    toasts.error('Saldo no disponible');
    await fixture.whenStable();
  });

  it('muestra los toasts en una región aria-live', () => {
    expect(messages()).toEqual(['Cliente creado', 'Saldo no disponible']);
    expect(queryAll(fixture, '[aria-live="polite"] .toast--error')).toHaveLength(1);
  });

  it('cierra un toast con su botón', async () => {
    queryAll<HTMLButtonElement>(fixture, 'button[aria-label="Cerrar notificación"]')[0].click();
    await fixture.whenStable();

    expect(messages()).toEqual(['Saldo no disponible']);
  });
});
