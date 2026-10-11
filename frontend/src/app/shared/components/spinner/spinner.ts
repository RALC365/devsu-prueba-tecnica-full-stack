import { ChangeDetectionStrategy, Component } from '@angular/core';

// las mismas tres barras del isologo, subiendo y bajando
@Component({
  selector: 'app-spinner',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'spinner', 'aria-hidden': 'true' },
  template: '<i></i><i></i><i></i>',
  styles: `
    :host {
      display: inline-flex;
      align-items: flex-end;
      justify-content: center;
      gap: 0.14em;
      width: 1em;
      height: 0.9em;
    }

    i {
      width: 0.2em;
      height: 100%;
      background-color: currentcolor;
      border-radius: 0.1em;
      transform-origin: center bottom;

      &:nth-child(1) {
        height: 50%;
      }

      &:nth-child(2) {
        height: 75%;
      }
    }

    @media (prefers-reduced-motion: no-preference) {
      i {
        animation: spinner-bars 900ms cubic-bezier(0.77, 0, 0.175, 1) infinite;

        &:nth-child(2) {
          animation-delay: 120ms;
        }

        &:nth-child(3) {
          animation-delay: 240ms;
        }
      }
    }

    @media (prefers-reduced-motion: reduce) {
      :host {
        animation: spinner-fade 1.2s ease-in-out infinite alternate;
      }
    }

    @keyframes spinner-bars {
      0%,
      100% {
        transform: scaleY(1);
      }

      50% {
        transform: scaleY(0.35);
      }
    }

    @keyframes spinner-fade {
      to {
        opacity: 0.4;
      }
    }
  `,
})
export class Spinner {}
