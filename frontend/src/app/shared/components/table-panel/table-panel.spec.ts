import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { buttonByText, normalizeSpaces } from '../../../../testing/dom';
import { TablePanel } from './table-panel';

@Component({
  imports: [TablePanel],
  template: `
    <app-table-panel
      emptyMessage="No hay clientes registrados"
      [total]="total()"
      [matches]="matches()"
      [loading]="loading()"
      [error]="error()"
      (retry)="events.push('retry')"
      (clearSearch)="events.push('clearSearch')"
    >
      <table id="tabla"></table>
    </app-table-panel>
  `,
})
class Host {
  readonly total = signal(0);
  readonly matches = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly events: string[] = [];
}

describe('TablePanel', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;

  const content = () => normalizeSpaces((fixture.nativeElement as HTMLElement).textContent ?? '');
  const table = () => (fixture.nativeElement as HTMLElement).querySelector('#tabla');

  async function render(
    state: Partial<{ total: number; matches: number; loading: boolean; error: string }>,
  ) {
    host.total.set(state.total ?? 0);
    host.matches.set(state.matches ?? 0);
    host.loading.set(state.loading ?? false);
    host.error.set(state.error ?? null);
    await fixture.whenStable();
  }

  beforeEach(() => {
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
  });

  it('muestra cargando en la primera carga', async () => {
    await render({ loading: true });

    expect(content()).toContain('Cargando');
    expect(table()).toBeNull();
  });

  it('muestra el error y permite reintentar', async () => {
    await render({ error: 'No se pudo conectar con el servidor' });

    expect(content()).toContain('No se pudo cargar la información');
    expect(content()).toContain('No se pudo conectar con el servidor');
    buttonByText(fixture, 'Reintentar').click();
    expect(host.events).toEqual(['retry']);
  });

  it('muestra el mensaje de lista vacía', async () => {
    await render({});

    expect(content()).toContain('No hay clientes registrados');
  });

  it('sin resultados permite limpiar la búsqueda', async () => {
    await render({ total: 3 });

    expect(content()).toContain('No se encontraron resultados');
    buttonByText(fixture, 'Limpiar búsqueda').click();
    expect(host.events).toEqual(['clearSearch']);
  });

  it('muestra la tabla si hay filas, aunque esté recargando', async () => {
    await render({ total: 3, matches: 2, loading: true });

    expect(table()).not.toBeNull();
  });
});
