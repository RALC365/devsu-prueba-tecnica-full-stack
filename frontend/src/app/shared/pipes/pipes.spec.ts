import { DateTimePipe } from './date-time-pipe';
import { MoneyPipe } from './money-pipe';

describe('MoneyPipe', () => {
  const pipe = new MoneyPipe();

  it('formatea montos simples y con signo', () => {
    expect(pipe.transform(1425)).toBe('$1,425.00');
    expect(pipe.transform(600, 'signed')).toBe('+$600.00');
    expect(pipe.transform(-575, 'signed')).toBe('-$575.00');
  });
});

describe('DateTimePipe', () => {
  it('formatea fechas y horas locales', () => {
    expect(new DateTimePipe().transform('2022-02-08T09:05:00')).toBe('08/02/2022 09:05');
  });
});
