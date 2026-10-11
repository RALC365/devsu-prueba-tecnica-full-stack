import { formatCurrency, formatDate } from '@angular/common';

const LOCALE = 'en-US';
const DATE_TIME_FORMAT = 'dd/MM/yyyy HH:mm';
const ISO_DATE_FORMAT = 'yyyy-MM-dd';

export function formatMoney(value: number): string {
  return formatCurrency(value, LOCALE, '$', 'USD');
}

export function formatSignedMoney(value: number): string {
  const amount = formatMoney(Math.abs(value));
  if (value > 0) {
    return `+${amount}`;
  }
  return value < 0 ? `-${amount}` : amount;
}

export function formatDateTime(value: string): string {
  return formatDate(value, DATE_TIME_FORMAT, LOCALE);
}

// en hora local; toISOString() la pasaría a UTC
export function toIsoDate(date: Date): string {
  return formatDate(date, ISO_DATE_FORMAT, LOCALE);
}
