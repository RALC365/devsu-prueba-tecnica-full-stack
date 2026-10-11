import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  afterNextRender,
  inject,
  input,
  output,
  viewChild,
} from '@angular/core';

import { Icon } from '../icon/icon';

const FOCUSABLE_SELECTOR = [
  'a[href]',
  'button:not([disabled])',
  'input:not([disabled])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(',');

let nextId = 0;

@Component({
  selector: 'app-modal',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon],
  host: {
    '[class.modal-host--drawer]': "variant() === 'drawer'",
    'animate.leave': 'modal-host--leaving',
    // en document porque el foco puede volver a <body> mientras se guarda
    '(document:keydown)': 'onKeydown($event)',
    '(mousedown)': 'onBackdropPress($event)',
    '(click)': 'onBackdropClick($event)',
  },
  templateUrl: './modal.html',
  styleUrl: './modal.scss',
})
export class Modal {
  readonly heading = input.required<string>();
  readonly size = input<'md' | 'sm'>('md');
  readonly variant = input<'dialog' | 'drawer'>('dialog');
  readonly closed = output();

  protected readonly titleId = `modal-title-${nextId++}`;
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly panel = viewChild.required<ElementRef<HTMLElement>>('panel');
  private readonly body = viewChild.required<ElementRef<HTMLElement>>('body');
  private readonly document = inject(DOCUMENT);
  private pressedOnBackdrop = false;

  constructor() {
    const opener = this.document.activeElement;
    const body = this.document.body;
    const scrollbarWidth =
      (this.document.defaultView?.innerWidth ?? 0) - this.document.documentElement.clientWidth;
    body.style.setProperty('--scrollbar-width', `${Math.max(0, scrollbarWidth)}px`);
    body.classList.add('has-modal');

    afterNextRender(() => this.focusFirstElement());

    inject(DestroyRef).onDestroy(() => {
      body.classList.remove('has-modal');
      body.style.removeProperty('--scrollbar-width');
      if (opener instanceof HTMLElement) {
        opener.focus();
      }
    });
  }

  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.closed.emit();
    } else if (event.key === 'Tab') {
      this.trapFocus(event);
    }
  }

  // Solo cierra si el clic empezó y terminó en el fondo: al seleccionar texto
  // de un campo y soltar afuera, el click también llega al fondo.
  protected onBackdropPress(event: MouseEvent): void {
    this.pressedOnBackdrop = event.target === this.host;
  }

  protected onBackdropClick(event: MouseEvent): void {
    if (this.pressedOnBackdrop && event.target === this.host) {
      this.closed.emit();
    }
    this.pressedOnBackdrop = false;
  }

  private focusFirstElement(): void {
    const target = this.body().nativeElement.querySelector<HTMLElement>(FOCUSABLE_SELECTOR);
    (target ?? this.panel().nativeElement).focus({ preventScroll: true });
  }

  private trapFocus(event: KeyboardEvent): void {
    const panel = this.panel().nativeElement;
    const focusable = Array.from(panel.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR));
    const first = focusable[0] ?? panel;
    const last = focusable[focusable.length - 1] ?? panel;
    const active = this.document.activeElement;
    const isOutside = !panel.contains(active);

    if (event.shiftKey && (isOutside || active === first || active === panel)) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && (isOutside || active === last)) {
      event.preventDefault();
      first.focus();
    }
  }
}
