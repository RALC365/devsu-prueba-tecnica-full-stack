import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { query, textOf, typeInto } from '../../../../testing/dom';
import { QuickSearch } from './quick-search';

@Component({
  imports: [QuickSearch],
  template: `<app-quick-search
    label="Buscar clientes"
    [(term)]="term"
    [total]="3"
    [matches]="1"
  />`,
})
class Host {
  readonly term = signal('');
}

describe('QuickSearch', () => {
  let fixture: ComponentFixture<Host>;
  let input: HTMLInputElement;

  beforeEach(async () => {
    fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    input = query<HTMLInputElement>(fixture, 'input[type="search"]');
  });

  it('tiene label, placeholder y contador', () => {
    expect(query(fixture, `label[for="${input.id}"]`).textContent).toBe('Buscar clientes');
    expect(input.placeholder).toBe('Buscar');
    expect(textOf(fixture, '[aria-live="polite"]')).toBe('1 de 3 registros');
  });

  it('actualiza term al escribir', async () => {
    typeInto(input, 'lema');
    await fixture.whenStable();

    expect(fixture.componentInstance.term()).toBe('lema');
  });

  it('el botón limpiar borra el texto y deja el foco', async () => {
    fixture.componentInstance.term.set('lema');
    await fixture.whenStable();

    query<HTMLButtonElement>(fixture, 'button[aria-label="Limpiar búsqueda"]').click();
    await fixture.whenStable();

    expect(fixture.componentInstance.term()).toBe('');
    expect(input.value).toBe('');
    expect(document.activeElement).toBe(input);
  });

  it('Escape borra el texto', async () => {
    typeInto(input, 'lema');
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await fixture.whenStable();

    expect(fixture.componentInstance.term()).toBe('');
  });
});
