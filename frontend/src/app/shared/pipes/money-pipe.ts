import { Pipe, PipeTransform } from '@angular/core';

import { formatMoney, formatSignedMoney } from '../utils/formatters';

@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  transform(value: number, display: 'plain' | 'signed' = 'plain'): string {
    return display === 'signed' ? formatSignedMoney(value) : formatMoney(value);
  }
}
