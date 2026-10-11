import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  model,
  viewChild,
} from '@angular/core';

import { Icon } from '../icon/icon';

const EDITABLE_SELECTOR = 'input, textarea, select, [contenteditable="true"]';

let nextId = 0;

@Component({
  selector: 'app-quick-search',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  host: { '(document:keydown)': 'onDocumentKeydown($event)' },
  templateUrl: './quick-search.html',
  styleUrl: './quick-search.scss',
})
export class QuickSearch {
  readonly term = model('');
  readonly total = input.required<number>();
  readonly matches = input.required<number>();
  readonly label = input('Buscar');

  protected readonly inputId = `quick-search-${nextId++}`;
  private readonly input = viewChild.required<ElementRef<HTMLInputElement>>('searchInput');
  private readonly document = inject(DOCUMENT);

  protected clear(input: HTMLInputElement): void {
    this.term.set('');
    input.focus();
  }

  protected onEscape(input: HTMLInputElement): void {
    if (this.term()) {
      this.term.set('');
    } else {
      input.blur();
    }
  }

  // "/" lleva el foco a la búsqueda, salvo que se esté escribiendo en otro campo
  protected onDocumentKeydown(event: KeyboardEvent): void {
    if (event.key !== '/' || event.ctrlKey || event.metaKey || event.altKey) {
      return;
    }
    const target = event.target instanceof Element ? event.target : null;
    if (target?.closest(EDITABLE_SELECTOR) || this.document.body.classList.contains('has-modal')) {
      return;
    }
    event.preventDefault();
    this.input().nativeElement.focus();
  }
}
