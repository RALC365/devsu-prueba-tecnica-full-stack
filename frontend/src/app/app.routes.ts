import { Routes } from '@angular/router';

import { Shell } from './layout/shell/shell';

export const routes: Routes = [
  {
    path: '',
    component: Shell,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'clientes' },
      {
        path: 'clientes',
        title: 'Clientes | BANCO',
        loadComponent: () =>
          import('./features/clientes/clientes-page/clientes-page').then((m) => m.ClientesPage),
      },
      {
        path: 'cuentas',
        title: 'Cuentas | BANCO',
        loadComponent: () =>
          import('./features/cuentas/cuentas-page/cuentas-page').then((m) => m.CuentasPage),
      },
      {
        path: 'movimientos',
        title: 'Movimientos | BANCO',
        loadComponent: () =>
          import('./features/movimientos/movimientos-page/movimientos-page').then(
            (m) => m.MovimientosPage,
          ),
      },
      {
        path: 'reportes',
        title: 'Reportes | BANCO',
        loadComponent: () =>
          import('./features/reportes/reportes-page/reportes-page').then((m) => m.ReportesPage),
      },
    ],
  },
  { path: '**', redirectTo: 'clientes' },
];
