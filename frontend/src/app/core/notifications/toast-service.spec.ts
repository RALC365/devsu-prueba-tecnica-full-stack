import { TestBed } from '@angular/core/testing';

import { TOAST_DURATION_MS, ToastService } from './toast-service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    jest.useFakeTimers();
    service = TestBed.inject(ToastService);
  });

  afterEach(() => jest.useRealTimers());

  it('agrega toasts con ids distintos', () => {
    service.success('Cliente creado');
    service.error('Saldo no disponible');

    expect(service.toasts()).toEqual([
      { id: 1, type: 'success', message: 'Cliente creado' },
      { id: 2, type: 'error', message: 'Saldo no disponible' },
    ]);
  });

  it('cierra un toast', () => {
    service.success('Uno');
    service.success('Dos');

    service.dismiss(1);

    expect(service.toasts().map((toast) => toast.message)).toEqual(['Dos']);
  });

  it('cierra los toasts al terminar su duración', () => {
    service.success('Guardado');
    service.error('Falló');

    jest.advanceTimersByTime(TOAST_DURATION_MS.success);
    expect(service.toasts().map((toast) => toast.type)).toEqual(['error']);

    jest.advanceTimersByTime(TOAST_DURATION_MS.error - TOAST_DURATION_MS.success);
    expect(service.toasts()).toEqual([]);
  });
});
