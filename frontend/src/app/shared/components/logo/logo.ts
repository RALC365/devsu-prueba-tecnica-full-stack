import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  viewChildren,
} from '@angular/core';

import { canAnimate } from '../../utils/motion';

const BARS = [
  { x: 8, y: 17, height: 8 },
  { x: 13.9, y: 12.5, height: 12.5 },
  { x: 19.8, y: 7, height: 18 },
] as const;

@Component({
  selector: 'app-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'logo', '(mouseenter)': 'wave()' },
  template: `
    <svg
      class="logo__mark"
      viewBox="0 0 32 32"
      width="32"
      height="32"
      aria-hidden="true"
      focusable="false"
    >
      <rect class="logo__tile" width="32" height="32" rx="9" />
      @for (bar of bars; track bar.x) {
        <rect
          #bar
          class="logo__bar"
          [attr.x]="bar.x"
          [attr.y]="bar.y"
          width="4.2"
          [attr.height]="bar.height"
          rx="2.1"
          [style.--bar-index]="$index"
        />
      }
    </svg>
    <span class="logo__word">BANCO</span>
  `,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      gap: 0.75rem;
    }

    .logo__mark {
      flex-shrink: 0;
    }

    .logo__tile {
      fill: var(--ink-950);
    }

    .logo__bar {
      transform-box: fill-box;
      transform-origin: center bottom;
      fill: #7ea4dc;

      &:nth-of-type(3) {
        fill: var(--brand-100);
      }

      &:nth-of-type(4) {
        fill: var(--accent);
      }
    }

    .logo__word {
      font-size: 1.0625rem;
      font-weight: 700;
      letter-spacing: 0.16em;
      color: var(--ink-950);
    }

    @media (prefers-reduced-motion: no-preference) {
      .logo__bar {
        transition: transform 700ms var(--ease-out);
        transition-delay: calc(var(--bar-index) * 90ms + 150ms);

        @starting-style {
          transform: scaleY(0.15);
        }
      }
    }
  `,
})
export class Logo {
  protected readonly bars = BARS;
  private readonly barElements = viewChildren<ElementRef<SVGRectElement>>('bar');
  private readonly document = inject(DOCUMENT);

  // con la Web Animations API la ola termina aunque el puntero salga antes
  protected wave(): void {
    const bars = this.barElements().map((bar) => bar.nativeElement);
    if (!canAnimate(this.document) || bars.some((bar) => bar.getAnimations?.().length)) {
      return;
    }
    bars.forEach((bar, index) =>
      bar.animate(
        [{ transform: 'scaleY(1)' }, { transform: 'scaleY(0.45)' }, { transform: 'scaleY(1)' }],
        { duration: 560, delay: index * 70, easing: 'cubic-bezier(0.77, 0, 0.175, 1)' },
      ),
    );
  }
}
