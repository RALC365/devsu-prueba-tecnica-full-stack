import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { Icon, IconName } from '../icon/icon';

@Component({
  selector: 'app-empty-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  template: `
    <span class="empty-state__art" [class.empty-state__art--danger]="tone() === 'danger'">
      <app-icon [name]="icon()" />
    </span>
    <p class="empty-state__title">{{ heading() }}</p>
    @if (description()) {
      <p class="empty-state__description">{{ description() }}</p>
    }
    <div class="empty-state__actions"><ng-content /></div>
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-2);
      padding: var(--space-12) var(--space-6);
      text-align: center;
    }

    .empty-state__art {
      display: grid;
      place-items: center;
      width: 3.5rem;
      height: 3.5rem;
      margin-bottom: var(--space-3);
      font-size: 1.5rem;
      color: var(--brand-600);
      background-color: var(--brand-50);
      border-radius: 50%;
      box-shadow: 0 0 0 0.5rem rgb(239 244 251 / 55%);
    }

    .empty-state__art--danger {
      color: var(--debit);
      background-color: var(--debit-soft);
      box-shadow: 0 0 0 0.5rem rgb(252 236 234 / 55%);
    }

    .empty-state__title {
      font-size: var(--text-lg);
      font-weight: var(--weight-semibold);
      color: var(--ink-950);
    }

    .empty-state__description {
      max-width: 28rem;
      color: var(--ink-500);
    }

    .empty-state__actions {
      display: flex;
      gap: var(--space-3);
      margin-top: var(--space-3);

      &:empty {
        display: none;
      }
    }

    @media (prefers-reduced-motion: no-preference) {
      .empty-state__art {
        animation: empty-art-in 420ms var(--ease-out) both;
      }
    }

    @keyframes empty-art-in {
      from {
        opacity: 0;
        transform: scale(0.9);
      }
    }
  `,
})
export class EmptyState {
  readonly heading = input.required<string>();
  readonly description = input<string>();
  readonly icon = input<IconName>('inbox');
  readonly tone = input<'neutral' | 'danger'>('neutral');
}
