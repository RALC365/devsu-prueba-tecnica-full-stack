import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { ESTADO_CLIENTE, EstadoLabels, estadoLabel } from '../../../core/models';

@Component({
  selector: 'app-status-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="badge" [class.badge--active]="active()">{{ label() }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 0.375rem;
      padding: 0.125rem 0.625rem 0.125rem 0.5rem;
      font-size: var(--text-xs);
      font-weight: var(--weight-semibold);
      line-height: 1.5;
      white-space: nowrap;
      color: var(--ink-500);
      background-color: var(--surface-sunken);
      border-radius: var(--radius-pill);
      box-shadow: inset 0 0 0 1px rgb(11 27 46 / 6%);

      &::before {
        content: '';
        width: 0.375rem;
        height: 0.375rem;
        background-color: var(--ink-300);
        border-radius: 50%;
      }
    }

    .badge--active {
      color: var(--credit);
      background-color: var(--credit-soft);
      box-shadow: inset 0 0 0 1px rgb(18 120 75 / 12%);

      &::before {
        background-color: currentcolor;
        box-shadow: 0 0 0 3px rgb(18 120 75 / 14%);
      }
    }
  `,
})
export class StatusBadge {
  readonly active = input.required<boolean>();
  readonly labels = input<EstadoLabels>(ESTADO_CLIENTE);

  protected readonly label = computed(() => estadoLabel(this.active(), this.labels()));
}
