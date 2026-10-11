import { DOCUMENT } from '@angular/common';
import { DestroyRef, Directive, ElementRef, effect, inject, input, untracked } from '@angular/core';

import { canAnimate } from '../utils/motion';

const DURATION_MS = 900;

export type CountUpFormat = (value: number) => string;

// Muestra una cifra contando desde el valor anterior (o desde 0) cuando entra
// en pantalla. Si el valor no cambia no vuelve a contar.
@Directive({ selector: '[appCountUp]' })
export class CountUp {
  readonly value = input.required<number>({ alias: 'appCountUp' });
  readonly countUpFormat = input<CountUpFormat>((value) => String(Math.round(value)));

  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly document = inject(DOCUMENT);
  private shown: number | null = null;
  private frame = 0;
  private observer: IntersectionObserver | null = null;

  constructor() {
    effect(() => {
      const target = this.value();
      const format = this.countUpFormat();
      untracked(() => this.update(target, format));
    });
    inject(DestroyRef).onDestroy(() => this.stop());
  }

  private update(target: number, format: CountUpFormat): void {
    this.stop();
    const view = this.document.defaultView;
    if (!view || !canAnimate(this.document) || !('IntersectionObserver' in view)) {
      this.render(target, format);
      return;
    }
    if (this.shown === target) {
      this.render(target, format);
      return;
    }
    const from = this.shown ?? 0;
    this.render(from, format);
    this.observer = new view.IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          this.disconnect();
          this.tween(from, target, format, view);
        }
      },
      { threshold: 0.6 },
    );
    this.observer.observe(this.element);
  }

  private tween(from: number, target: number, format: CountUpFormat, view: Window): void {
    const start = view.performance.now();
    const step = (now: number) => {
      const progress = Math.min(1, Math.max(0, (now - start) / DURATION_MS));
      const eased = 1 - Math.pow(1 - progress, 4);
      this.render(progress < 1 ? from + (target - from) * eased : target, format);
      if (progress < 1) {
        this.frame = view.requestAnimationFrame(step);
      }
    };
    this.frame = view.requestAnimationFrame(step);
  }

  private render(value: number, format: CountUpFormat): void {
    this.shown = value;
    this.element.textContent = format(value);
  }

  private stop(): void {
    this.disconnect();
    if (this.frame) {
      this.document.defaultView?.cancelAnimationFrame(this.frame);
      this.frame = 0;
    }
  }

  private disconnect(): void {
    this.observer?.disconnect();
    this.observer = null;
  }
}
