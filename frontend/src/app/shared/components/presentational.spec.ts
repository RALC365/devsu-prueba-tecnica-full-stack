import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { ESTADO_CUENTA } from '../../core/models';
import { normalizeSpaces, queryAll } from '../../../testing/dom';
import { EmptyState } from './empty-state/empty-state';
import { ErrorAlert } from './error-alert/error-alert';
import { Icon } from './icon/icon';
import { PageHeader } from './page-header/page-header';
import { StatusBadge } from './status-badge/status-badge';

@Component({
  imports: [EmptyState, ErrorAlert, Icon, PageHeader, StatusBadge],
  template: `
    <app-page-header heading="Clientes" />
    <app-status-badge id="cliente" [active]="true" />
    <app-status-badge id="cuenta" [active]="false" [labels]="estadoCuenta" />
    <app-error-alert
      message="La solicitud contiene datos inválidos"
      [details]="['Cuenta inactiva']"
    />
    <app-empty-state
      heading="No se pudo cargar la información"
      description="No se pudo conectar con el servidor"
    >
      <button type="button">Reintentar</button>
    </app-empty-state>
    <app-icon id="icono" name="search" />
  `,
})
class Host {
  readonly estadoCuenta = ESTADO_CUENTA;
}

describe('componentes de presentación', () => {
  const render = async () => {
    const fixture = TestBed.createComponent(Host);
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;
    const text = (selector: string) =>
      normalizeSpaces(element.querySelector(selector)?.textContent ?? '');
    return { fixture, element, text };
  };

  it('PageHeader muestra el título', async () => {
    const { text } = await render();

    expect(text('h1')).toBe('Clientes');
  });

  it('StatusBadge usa las etiquetas de la entidad', async () => {
    const { element, text } = await render();

    expect(text('#cliente')).toBe('Activo');
    expect(text('#cuenta')).toBe('Inactiva');
    expect(element.querySelector('#cliente .badge')?.classList).toContain('badge--active');
  });

  it('ErrorAlert tiene role alert y lista los detalles', async () => {
    const { element, text } = await render();

    expect(element.querySelector('app-error-alert')?.getAttribute('role')).toBe('alert');
    expect(text('.error-alert__message')).toBe('La solicitud contiene datos inválidos');
    expect(text('.error-alert__details')).toBe('Cuenta inactiva');
  });

  it('EmptyState muestra título, descripción y acciones', async () => {
    const { text } = await render();

    expect(text('.empty-state__title')).toBe('No se pudo cargar la información');
    expect(text('.empty-state__description')).toBe('No se pudo conectar con el servidor');
    expect(text('app-empty-state button')).toBe('Reintentar');
  });

  it('Icon dibuja el SVG con aria-hidden', async () => {
    const { fixture, element } = await render();

    expect(element.querySelector('#icono')?.getAttribute('aria-hidden')).toBe('true');
    expect(queryAll(fixture, '#icono svg path').length).toBeGreaterThan(0);
  });
});
