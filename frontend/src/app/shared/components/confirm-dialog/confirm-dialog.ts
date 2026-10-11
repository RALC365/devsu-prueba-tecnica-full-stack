import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { Icon } from '../icon/icon';
import { Modal } from '../modal/modal';
import { Spinner } from '../spinner/spinner';

@Component({
  selector: 'app-confirm-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, Modal, Spinner],
  template: `
    <app-modal size="sm" [heading]="heading()" (closed)="cancelled.emit()">
      <div class="confirm">
        <span class="confirm__icon"><app-icon name="trash" /></span>
        <div class="confirm__message"><ng-content /></div>
      </div>
      <div class="form-actions">
        <button
          type="button"
          class="btn btn--secondary"
          [disabled]="busy()"
          (click)="cancelled.emit()"
        >
          Cancelar
        </button>
        <button
          type="button"
          class="btn btn--danger"
          [disabled]="busy()"
          (click)="confirmed.emit()"
        >
          @if (busy()) {
            <app-spinner />
          } @else {
            <app-icon name="trash" />
          }
          {{ confirmLabel() }}
        </button>
      </div>
    </app-modal>
  `,
  styles: `
    .confirm {
      display: flex;
      gap: var(--space-4);
    }

    .confirm__icon {
      display: grid;
      place-items: center;
      flex-shrink: 0;
      width: 2.75rem;
      height: 2.75rem;
      font-size: 1.25rem;
      color: var(--debit);
      background-color: var(--debit-soft);
      border-radius: 50%;
    }

    .confirm__message {
      display: flex;
      flex-direction: column;
      gap: var(--space-1);
      align-self: center;
      color: var(--ink-600);
    }
  `,
})
export class ConfirmDialog {
  readonly heading = input.required<string>();
  readonly confirmLabel = input('Eliminar');
  readonly busy = input(false);
  readonly confirmed = output();
  readonly cancelled = output();
}
