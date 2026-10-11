import { getErrorMessage } from './validation-messages';

describe('getErrorMessage', () => {
  it('devuelve null si no hay errores', () => {
    expect(getErrorMessage(null)).toBeNull();
    expect(getErrorMessage({})).toBeNull();
  });

  it('arma los mensajes por defecto', () => {
    expect(getErrorMessage({ required: true })).toBe('Campo obligatorio');
    expect(getErrorMessage({ minlength: { requiredLength: 4, actualLength: 3 } })).toBe(
      'Mínimo 4 caracteres',
    );
    expect(getErrorMessage({ maxlength: { requiredLength: 100, actualLength: 101 } })).toBe(
      'Máximo 100 caracteres',
    );
    expect(getErrorMessage({ min: { min: 1, actual: 0 } })).toBe('Valor mínimo: 1');
    expect(getErrorMessage({ max: { max: 120, actual: 121 } })).toBe('Valor máximo: 120');
    expect(getErrorMessage({ maxDecimals: { max: 2 } })).toBe('Máximo 2 decimales');
    expect(getErrorMessage({ pattern: { requiredPattern: '^\\d+$' } })).toBe('Formato inválido');
    expect(getErrorMessage({ dateRange: true })).toBe(
      'La fecha de inicio no puede ser mayor a la fecha fin',
    );
    expect(getErrorMessage({ blank: true })).toBe('No puede contener solo espacios');
    expect(getErrorMessage({ maxBytes: { max: 72, actual: 74 } })).toBe('Máximo 72 bytes');
  });

  it('usa el mensaje propio si existe', () => {
    expect(getErrorMessage({ pattern: {} }, { pattern: 'Entre 7 y 15 dígitos' })).toBe(
      'Entre 7 y 15 dígitos',
    );
  });

  it('muestra solo el primer error', () => {
    expect(getErrorMessage({ required: true, min: { min: 1 } })).toBe('Campo obligatorio');
  });

  it('el mensaje del backend tiene prioridad', () => {
    expect(getErrorMessage({ required: true, server: 'El valor debe ser mayor a 0' })).toBe(
      'El valor debe ser mayor a 0',
    );
  });

  it('usa un mensaje genérico para errores desconocidos', () => {
    expect(getErrorMessage({ custom: true })).toBe('Valor inválido');
  });
});
