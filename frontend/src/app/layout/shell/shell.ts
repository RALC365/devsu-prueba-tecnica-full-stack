import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { filter, map } from 'rxjs';

import { Icon, IconName } from '../../shared/components/icon/icon';
import { Logo } from '../../shared/components/logo/logo';

interface NavItem {
  readonly path: string;
  readonly label: string;
  readonly icon: IconName;
}

const NAV_ITEMS: readonly NavItem[] = [
  { path: 'clientes', label: 'Clientes', icon: 'users' },
  { path: 'cuentas', label: 'Cuentas', icon: 'credit-card' },
  { path: 'movimientos', label: 'Movimientos', icon: 'transfer' },
  { path: 'reportes', label: 'Reportes', icon: 'chart' },
];

@Component({
  selector: 'app-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, Logo, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  protected readonly navItems = NAV_ITEMS;
  private readonly route = inject(ActivatedRoute);

  // índice de la sección actual; mueve el indicador del menú
  protected readonly activeIndex = toSignal(
    inject(Router).events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => this.currentIndex()),
    ),
    { initialValue: this.currentIndex() },
  );

  // routeConfig ya existe al crear el shell; snapshot se asigna después
  private currentIndex(): number {
    const path = this.route.firstChild?.routeConfig?.path;
    return NAV_ITEMS.findIndex((item) => item.path === path);
  }
}
