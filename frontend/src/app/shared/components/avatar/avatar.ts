import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

const TONES = 5;

// las iniciales van en ::before para no ensuciar el texto de la celda
@Component({
  selector: 'app-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'avatar',
    'aria-hidden': 'true',
    '[attr.data-initials]': 'initials()',
    '[attr.data-tone]': 'tone()',
  },
  template: '',
  styles: `
    :host {
      display: inline-grid;
      flex-shrink: 0;
      place-items: center;
      width: 2rem;
      height: 2rem;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.02em;
      border-radius: 50%;

      &::before {
        content: attr(data-initials);
      }
    }

    :host([data-tone='0']) {
      color: #1a4f99;
      background-color: #e3ecfa;
    }

    :host([data-tone='1']) {
      color: #8f5408;
      background-color: #fbeedd;
    }

    :host([data-tone='2']) {
      color: #0f6e74;
      background-color: #ddf1f2;
    }

    :host([data-tone='3']) {
      color: #5a3fae;
      background-color: #ece8fb;
    }

    :host([data-tone='4']) {
      color: #3d4f66;
      background-color: #e8ecf2;
    }
  `,
})
export class Avatar {
  readonly name = input.required<string>();

  protected readonly initials = computed(() =>
    this.name()
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((word) => word.charAt(0).toUpperCase())
      .join(''),
  );

  protected readonly tone = computed(() => {
    let hash = 0;
    for (const char of this.name()) {
      hash = (hash * 31 + char.charCodeAt(0)) % 997;
    }
    return hash % TONES;
  });
}
