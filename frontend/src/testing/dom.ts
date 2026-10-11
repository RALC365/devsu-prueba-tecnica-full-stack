import { ComponentFixture } from '@angular/core/testing';

function root(fixture: ComponentFixture<unknown>): HTMLElement {
  return fixture.nativeElement as HTMLElement;
}

export function query<T extends Element = HTMLElement>(
  fixture: ComponentFixture<unknown>,
  selector: string,
): T {
  const element = root(fixture).querySelector<T>(selector);
  if (!element) {
    throw new Error(`No element matches "${selector}"`);
  }
  return element;
}

export function queryAll<T extends Element = HTMLElement>(
  fixture: ComponentFixture<unknown>,
  selector: string,
): T[] {
  return Array.from(root(fixture).querySelectorAll<T>(selector));
}

export function textOf(fixture: ComponentFixture<unknown>, selector: string): string {
  return normalizeSpaces(query(fixture, selector).textContent ?? '');
}

export function tableRows(fixture: ComponentFixture<unknown>, selector = 'table'): string[][] {
  return queryAll(fixture, `${selector} tbody tr`).map((row) =>
    Array.from(row.querySelectorAll('td')).map((cell) => normalizeSpaces(cell.textContent ?? '')),
  );
}

export function buttonByText(fixture: ComponentFixture<unknown>, text: string): HTMLButtonElement {
  const button = queryAll<HTMLButtonElement>(fixture, 'button').find((candidate) =>
    normalizeSpaces(candidate.textContent ?? '').includes(text),
  );
  if (!button) {
    throw new Error(`No button with text "${text}"`);
  }
  return button;
}

export function typeInto(input: HTMLInputElement, value: string): void {
  input.value = value;
  input.dispatchEvent(new Event('input'));
  input.dispatchEvent(new Event('blur'));
}

// sirve para un <select> o para un grupo de radios (selector segmentado)
export function selectOption(control: HTMLElement, label: string): void {
  if (control instanceof HTMLSelectElement) {
    const option = Array.from(control.options).find((candidate) =>
      normalizeSpaces(candidate.textContent ?? '').includes(label),
    );
    if (!option) {
      throw new Error(`No option with label "${label}"`);
    }
    control.value = option.value;
    control.dispatchEvent(new Event('change'));
    return;
  }
  const radio = Array.from(control.querySelectorAll('label'))
    .find((candidate) => normalizeSpaces(candidate.textContent ?? '').includes(label))
    ?.querySelector<HTMLInputElement>('input[type="radio"]');
  if (!radio) {
    throw new Error(`No option with label "${label}"`);
  }
  radio.click();
}

export function normalizeSpaces(text: string): string {
  return text.replace(/\s+/g, ' ').trim();
}
