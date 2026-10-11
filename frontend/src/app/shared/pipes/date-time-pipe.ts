import { Pipe, PipeTransform } from '@angular/core';

import { formatDateTime } from '../utils/formatters';

@Pipe({ name: 'dateTime' })
export class DateTimePipe implements PipeTransform {
  transform(value: string): string {
    return formatDateTime(value);
  }
}
