import { formatDateTime, formatMoney, formatSignedMoney, toIsoDate } from './formatters';

describe('formatters', () => {
  it('formatea montos en USD', () => {
    expect(formatMoney(1425)).toBe('$1,425.00');
    expect(formatMoney(0.5)).toBe('$0.50');
    expect(formatMoney(-540)).toBe('-$540.00');
  });

  it('formatea montos con signo', () => {
    expect(formatSignedMoney(600)).toBe('+$600.00');
    expect(formatSignedMoney(-575)).toBe('-$575.00');
    expect(formatSignedMoney(0)).toBe('$0.00');
  });

  it('formatea fecha y hora', () => {
    expect(formatDateTime('2022-02-10T14:30:45')).toBe('10/02/2022 14:30');
  });

  it('toIsoDate usa la hora local', () => {
    expect(toIsoDate(new Date(2026, 0, 5, 23, 59))).toBe('2026-01-05');
  });
});
