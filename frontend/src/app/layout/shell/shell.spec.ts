import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { normalizeSpaces } from '../../../testing/dom';
import { Shell } from './shell';

@Component({ template: '<p id="pagina">Contenido de la página</p>' })
class FakePage {}

describe('Shell', () => {
  async function navigate(url: string): Promise<HTMLElement> {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          {
            path: '',
            component: Shell,
            children: ['clientes', 'cuentas', 'movimientos', 'reportes'].map((path) => ({
              path,
              component: FakePage,
            })),
          },
        ]),
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    return harness.routeNativeElement as HTMLElement;
  }

  const links = (root: HTMLElement) =>
    Array.from(root.querySelectorAll<HTMLAnchorElement>('nav a'));

  it('muestra la marca, el menú y la página', async () => {
    const root = await navigate('/clientes');

    expect(normalizeSpaces(root.querySelector('header')?.textContent ?? '')).toBe('BANCO');
    expect(root.querySelector('nav')?.getAttribute('aria-label')).toBe('Menú principal');
    expect(
      links(root).map((link) => [
        normalizeSpaces(link.textContent ?? ''),
        link.getAttribute('href'),
      ]),
    ).toEqual([
      ['Clientes', '/clientes'],
      ['Cuentas', '/cuentas'],
      ['Movimientos', '/movimientos'],
      ['Reportes', '/reportes'],
    ]);
    expect(root.querySelector('main #pagina')).not.toBeNull();
  });

  it('marca el link activo', async () => {
    const root = await navigate('/movimientos');

    const active = links(root).filter((link) => link.classList.contains('nav__link--active'));
    expect(active.map((link) => normalizeSpaces(link.textContent ?? ''))).toEqual(['Movimientos']);
    expect(active[0].getAttribute('aria-current')).toBe('page');
  });
});
