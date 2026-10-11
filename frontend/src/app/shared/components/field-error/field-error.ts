import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl } from '@angular/forms';
import { map, startWith, switchMap } from 'rxjs';

import { ValidationMessages, getErrorMessage } from '../../forms/validation-messages';

@Component({
  selector: 'app-field-error',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (message(); as message) {
      <span class="form-error">{{ message }}</span>
    }
  `,
})
export class FieldError {
  readonly control = input.required<AbstractControl>();
  readonly messages = input<ValidationMessages>({});

  private readonly visibleErrors = toSignal(
    toObservable(this.control).pipe(
      switchMap((control) =>
        control.events.pipe(
          startWith(null),
          map(() => (control.invalid && control.touched ? control.errors : null)),
        ),
      ),
    ),
    { initialValue: null },
  );

  protected readonly message = computed(() =>
    getErrorMessage(this.visibleErrors(), this.messages()),
  );
}
