import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { routes } from './app.routes';

describe('rutas', () => {
  let harness: RouterTestingHarness;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
    harness = await RouterTestingHarness.create();
  });

  it.each([
    ['/clientes', 'app-clientes-page', 'Clientes | BANCO'],
    ['/cuentas', 'app-cuentas-page', 'Cuentas | BANCO'],
    ['/movimientos', 'app-movimientos-page', 'Movimientos | BANCO'],
    ['/reportes', 'app-reportes-page', 'Reportes | BANCO'],
  ])('carga %s dentro del shell', async (url, selector, title) => {
    await harness.navigateByUrl(url);
    const shell = harness.routeNativeElement as HTMLElement;

    expect(shell.querySelector(`main ${selector}`)).not.toBeNull();
    expect(document.title).toBe(title);
  });

  it.each(['/', '/no-existe'])('redirige %s a clientes', async (url) => {
    await harness.navigateByUrl(url);

    expect(TestBed.inject(Router).url).toBe('/clientes');
  });
});
