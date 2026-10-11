import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { TOAST_DURATION_MS, ToastService } from '../../../core/notifications/toast-service';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-toast-container',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.scss',
})
export class ToastContainer {
  protected readonly toastService = inject(ToastService);
  protected readonly durations = TOAST_DURATION_MS;
}
