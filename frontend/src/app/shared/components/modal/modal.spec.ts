import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { query } from '../../../../testing/dom';
import { Modal } from './modal';

@Component({
  imports: [Modal],
  template: `
    <button id="opener" type="button">Abrir</button>
    @if (open()) {
      <app-modal heading="Nuevo cliente" (closed)="closedCount = closedCount + 1">
        <input id="first" />
        <button id="last" type="button">Guardar</button>
      </app-modal>
    }
  `,
})
class Host {
  readonly open = signal(false);
  closedCount = 0;
}

describe('Modal', () => {
  let fixture: ComponentFixture<Host>;

  async function openModal(): Promise<void> {
    query<HTMLButtonElement>(fixture, '#opener').focus();
    fixture.componentInstance.open.set(true);
    await fixture.whenStable();
  }

  function pressKey(key: string, shiftKey = false): KeyboardEvent {
    const event = new KeyboardEvent('keydown', { key, shiftKey, bubbles: true, cancelable: true });
    document.activeElement?.dispatchEvent(event);
    return event;
  }

  beforeEach(async () => {
    fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    await openModal();
  });

  it('tiene role dialog y aria-labelledby', () => {
    const dialog = query(fixture, '[role="dialog"]');
    const titleId = dialog.getAttribute('aria-labelledby') ?? '';

    expect(dialog.getAttribute('aria-modal')).toBe('true');
    expect(document.getElementById(titleId)?.textContent).toBe('Nuevo cliente');
  });

  it('enfoca el primer campo y bloquea el scroll', () => {
    expect(document.activeElement).toBe(query(fixture, '#first'));
    expect(document.body.classList).toContain('has-modal');
  });

  it('emite closed con Escape y con el botón cerrar', () => {
    pressKey('Escape');
    query<HTMLButtonElement>(fixture, 'button[aria-label="Cerrar"]').click();

    expect(fixture.componentInstance.closedCount).toBe(2);
  });

  it('mantiene el foco adentro con Tab', () => {
    query<HTMLButtonElement>(fixture, '#last').focus();
    const forward = pressKey('Tab');
    expect(forward.defaultPrevented).toBe(true);
    expect(document.activeElement).toBe(query(fixture, 'button[aria-label="Cerrar"]'));

    const backward = pressKey('Tab', true);
    expect(backward.defaultPrevented).toBe(true);
    expect(document.activeElement).toBe(query(fixture, '#last'));
  });

  it('recupera el foco si quedó afuera', () => {
    (document.activeElement as HTMLElement).blur();

    pressKey('Tab');

    expect(document.activeElement).toBe(query(fixture, 'button[aria-label="Cerrar"]'));
  });

  it('al cerrar devuelve el foco y desbloquea el scroll', async () => {
    fixture.componentInstance.open.set(false);
    await fixture.whenStable();

    expect(document.activeElement).toBe(query(fixture, '#opener'));
    expect(document.body.classList).not.toContain('has-modal');
  });
});
