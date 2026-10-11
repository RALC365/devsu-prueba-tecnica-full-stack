import { FormControl, FormGroup } from '@angular/forms';

import { dateRange, maxBytes, maxDecimals, notBlank } from './validators';

describe('maxDecimals', () => {
  const validator = maxDecimals(2);
  const validate = (value: unknown) => validator(new FormControl(value));

  it('acepta hasta 2 decimales y vacío', () => {
    expect(validate(10)).toBeNull();
    expect(validate(10.5)).toBeNull();
    expect(validate(10.25)).toBeNull();
    expect(validate(null)).toBeNull();
  });

  it('rechaza más decimales', () => {
    expect(validate(10.255)).toEqual({ maxDecimals: { max: 2 } });
  });
});

describe('notBlank', () => {
  const validate = (value: unknown) => notBlank(new FormControl(value));

  it('rechaza solo espacios', () => {
    expect(validate('    ')).toEqual({ blank: true });
    expect(validate('\t \n')).toEqual({ blank: true });
  });

  it('acepta texto y deja el vacío a required', () => {
    expect(validate(' clave ')).toBeNull();
    expect(validate('')).toBeNull();
    expect(validate(null)).toBeNull();
  });
});

describe('maxBytes', () => {
  const validate = (value: unknown) => maxBytes(72)(new FormControl(value));

  it('acepta hasta 72 bytes', () => {
    expect(validate('a'.repeat(72))).toBeNull();
    expect(validate('ñ'.repeat(36))).toBeNull();
    expect(validate(null)).toBeNull();
  });

  it('cuenta bytes UTF-8 y no caracteres', () => {
    expect(validate('a'.repeat(73))).toEqual({ maxBytes: { max: 72, actual: 73 } });
    expect(validate('ñ'.repeat(37))).toEqual({ maxBytes: { max: 72, actual: 74 } });
  });
});

describe('dateRange', () => {
  const form = (fechaInicio: string, fechaFin: string) =>
    new FormGroup(
      { fechaInicio: new FormControl(fechaInicio), fechaFin: new FormControl(fechaFin) },
      { validators: dateRange('fechaInicio', 'fechaFin') },
    );

  it('acepta inicio menor o igual a fin', () => {
    expect(form('2022-02-01', '2022-02-28').errors).toBeNull();
    expect(form('2022-02-28', '2022-02-28').errors).toBeNull();
  });

  it('devuelve dateRange si inicio es mayor a fin', () => {
    expect(form('2022-03-01', '2022-02-28').errors).toEqual({ dateRange: true });
  });

  it('ignora fechas vacías', () => {
    expect(form('', '2022-02-28').errors).toBeNull();
  });
});
