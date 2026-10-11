import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { rxResource } from '@angular/core/rxjs-interop';
import { Observable, of, throwError } from 'rxjs';

import { ApiError } from '../../core/http/api-error';
import { toLookup } from './lookup';

describe('toLookup', () => {
  function lookupOf(stream: Observable<readonly string[]>) {
    return TestBed.runInInjectionContext(() =>
      toLookup(rxResource({ stream: () => stream, defaultValue: [] })),
    );
  }

  const settle = () => TestBed.inject(ApplicationRef).whenStable();

  it('pasa de cargando a los datos', async () => {
    const lookup = lookupOf(of(['478758', '225487']));

    expect(lookup()).toEqual({ items: [], loading: true, error: null });

    await settle();

    expect(lookup()).toEqual({ items: ['478758', '225487'], loading: false, error: null });
  });

  it('expone el error sin lanzar', async () => {
    const lookup = lookupOf(throwError(() => new ApiError(500, 'Ocurrió un error inesperado')));

    await settle();

    expect(lookup()).toEqual({ items: [], loading: false, error: 'Ocurrió un error inesperado' });
  });
});
