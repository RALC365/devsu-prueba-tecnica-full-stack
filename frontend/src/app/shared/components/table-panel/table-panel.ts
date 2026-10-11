import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { EmptyState } from '../empty-state/empty-state';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-table-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EmptyState, Icon],
  templateUrl: './table-panel.html',
  host: { class: 'card' },
  styles: `
    :host {
      display: block;
      overflow: hidden;
    }

    .skeleton-table {
      padding: var(--space-2) 0;
    }

    .skeleton-table__row {
      display: flex;
      align-items: center;
      gap: var(--space-8);
      height: 3.5rem;
      padding: 0 var(--space-5);
      border-bottom: 1px solid var(--line-soft);

      &:last-child {
        border-bottom: 0;
      }
    }

    .skeleton {
      display: block;
      width: 24%;
      height: 0.625rem;
      background: linear-gradient(90deg, #edf0f5 0%, #f6f8fb 45%, #edf0f5 90%) 0 0 / 200% 100%;
      border-radius: var(--radius-pill);

      &:nth-child(3) {
        width: 14%;
      }

      &:nth-child(4) {
        width: 22%;
      }

      &:nth-child(5) {
        width: 10%;
      }
    }

    .skeleton--circle {
      flex-shrink: 0;
      width: 2rem;
      height: 2rem;
      margin-right: calc(-1 * var(--space-5));
    }

    @media (max-width: 599.98px) {
      .skeleton--wide {
        display: none;
      }
    }

    @media (prefers-reduced-motion: no-preference) {
      .skeleton {
        animation: skeleton-sweep 1.4s linear infinite;
      }
    }

    @keyframes skeleton-sweep {
      to {
        background-position: -200% 0;
      }
    }
  `,
})
export class TablePanel {
  readonly total = input.required<number>();
  readonly matches = input.required<number>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  readonly emptyMessage = input('No hay registros');
  readonly createLabel = input<string>();
  readonly retry = output();
  readonly clearSearch = output();
  readonly create = output();

  protected readonly skeletonRows = [1, 2, 3, 4, 5];
}
