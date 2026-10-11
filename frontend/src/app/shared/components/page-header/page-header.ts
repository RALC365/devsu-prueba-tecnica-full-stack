import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page-header__text">
      <h1 class="page-header__title">{{ heading() }}</h1>
      <ng-content select=".page-header__meta" />
    </div>
    <div class="page-header__actions"><ng-content /></div>
  `,
  styles: `
    :host {
      display: flex;
      flex-wrap: wrap;
      align-items: flex-end;
      justify-content: space-between;
      gap: var(--space-3) var(--space-4);
      margin-bottom: var(--space-6);
    }

    .page-header__text {
      min-width: 0;
    }

    .page-header__title {
      font-size: var(--text-3xl);
      font-weight: var(--weight-bold);
      letter-spacing: -0.02em;
      line-height: 1.15;
    }

    .page-header__actions {
      display: flex;
      gap: var(--space-3);
      margin-left: auto;

      &:empty {
        display: none;
      }
    }

    @media (max-width: 599.98px) {
      :host {
        margin-bottom: var(--space-5);
      }

      .page-header__title {
        font-size: var(--text-2xl);
      }

      // en grilla los botones proyectados se estiran solos
      .page-header__actions {
        display: grid;
        flex: 1 1 100%;
        grid-auto-columns: minmax(0, 1fr);
        grid-auto-flow: column;
      }
    }
  `,
})
export class PageHeader {
  readonly heading = input.required<string>();
}
