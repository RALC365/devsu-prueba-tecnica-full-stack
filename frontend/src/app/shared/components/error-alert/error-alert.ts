import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { Icon } from '../icon/icon';

@Component({
  selector: 'app-error-alert',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  host: { role: 'alert' },
  template: `
    <app-icon class="error-alert__icon" name="alert-circle" />
    <div>
      <p class="error-alert__message">{{ message() }}</p>
      @if (details().length) {
        <ul class="error-alert__details">
          @for (detail of details(); track detail) {
            <li>{{ detail }}</li>
          }
        </ul>
      }
    </div>
  `,
  styles: `
    :host {
      display: flex;
      align-items: flex-start;
      gap: var(--space-3);
      padding: var(--space-3) var(--space-4);
      color: var(--debit-strong);
      background-color: var(--debit-soft);
      border-radius: var(--radius-md);
      box-shadow: inset 0 0 0 1px rgb(192 58 47 / 16%);
    }

    .error-alert__icon {
      margin-top: 0.125rem;
      font-size: 1.125rem;
    }

    .error-alert__message {
      font-weight: var(--weight-semibold);
    }

    .error-alert__details {
      margin: var(--space-1) 0 0;
      padding-left: var(--space-5);
    }

    @media (prefers-reduced-motion: no-preference) {
      :host {
        animation: alert-in 260ms var(--ease-out);
      }
    }

    @keyframes alert-in {
      from {
        opacity: 0;
        transform: translateY(-4px);
      }
    }
  `,
})
export class ErrorAlert {
  readonly message = input.required<string>();
  readonly details = input<readonly string[]>([]);
}
