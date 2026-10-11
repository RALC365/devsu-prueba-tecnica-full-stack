import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { buttonByText, query } from '../../../../testing/dom';
import { ConfirmDialog } from './confirm-dialog';

@Component({
  imports: [ConfirmDialog],
  template: `
    <app-confirm-dialog
      heading="Eliminar cliente"
      [busy]="busy()"
      (confirmed)="events.push('confirmed')"
      (cancelled)="events.push('cancelled')"
    >
      ¿Desea eliminar a <strong>José Lema</strong>?
    </app-confirm-dialog>
  `,
})
class Host {
  readonly busy = signal(false);
  readonly events: string[] = [];
}

describe('ConfirmDialog', () => {
  let fixture: ComponentFixture<Host>;

  beforeEach(async () => {
    fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
  });

  it('muestra el contenido proyectado', () => {
    expect(query(fixture, '[role="dialog"]').textContent).toContain('¿Desea eliminar a José Lema?');
  });

  it('emite confirmed o cancelled', () => {
    buttonByText(fixture, 'Eliminar').click();
    buttonByText(fixture, 'Cancelar').click();

    expect(fixture.componentInstance.events).toEqual(['confirmed', 'cancelled']);
  });

  it('enfoca Cancelar al abrir', () => {
    expect(document.activeElement).toBe(buttonByText(fixture, 'Cancelar'));
  });

  it('deshabilita los botones mientras está ocupado', async () => {
    fixture.componentInstance.busy.set(true);
    await fixture.whenStable();

    expect(buttonByText(fixture, 'Eliminar').disabled).toBe(true);
    expect(buttonByText(fixture, 'Cancelar').disabled).toBe(true);
  });
});
