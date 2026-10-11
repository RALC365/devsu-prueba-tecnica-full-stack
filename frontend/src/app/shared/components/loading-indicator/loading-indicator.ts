import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { Spinner } from '../spinner/spinner';

@Component({
  selector: 'app-loading-indicator',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Spinner],
  host: { role: 'status' },
  template: `<app-spinner /><span>{{ label() }}</span>`,
  styles: `
    :host {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: var(--space-3);
      padding: var(--space-12) var(--space-4);
      font-weight: var(--weight-medium);
      color: var(--ink-500);
    }

    app-spinner {
      font-size: 1.25rem;
      color: var(--brand-600);
    }
  `,
})
export class LoadingIndicator {
  readonly label = input('Cargando');
}
