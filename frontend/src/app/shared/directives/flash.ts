import { DOCUMENT } from '@angular/common';
import { Directive, ElementRef, effect, inject, input, untracked } from '@angular/core';

import { canAnimate } from '../utils/motion';

export interface Highlight {
  readonly id: number;
}

// alto del encabezado fijo más un margen
const TOP_OFFSET_PX = 80;

// Una fila recreada después (por ejemplo al limpiar la búsqueda) no debe
// volver a iluminarse, por eso se recuerda el último resaltado mostrado.
let lastShown: Highlight | null = null;

@Directive({ selector: '[appFlash]' })
export class Flash {
  readonly highlight = input<Highlight | null>(null, { alias: 'appFlash' });
  readonly id = input.required<number>({ alias: 'appFlashId' });

  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly document = inject(DOCUMENT);

  constructor() {
    effect(() => {
      const highlight = this.highlight();
      if (highlight && highlight !== lastShown && highlight.id === this.id()) {
        lastShown = highlight;
        untracked(() => this.flash());
      }
    });
  }

  private flash(): void {
    this.element.classList.remove('row-flash');
    // forzar un reflow reinicia la animación si la fila ya la tenía
    void this.element.offsetWidth;
    this.element.classList.add('row-flash');
    this.revealIfHidden();
  }

  private revealIfHidden(): void {
    const view = this.document.defaultView;
    if (!view || typeof this.element.scrollIntoView !== 'function') {
      return;
    }
    const { top, bottom } = this.element.getBoundingClientRect();
    if (top < TOP_OFFSET_PX || bottom > view.innerHeight) {
      this.element.scrollIntoView({
        block: 'center',
        behavior: canAnimate(this.document) ? 'smooth' : 'auto',
      });
    }
  }
}
