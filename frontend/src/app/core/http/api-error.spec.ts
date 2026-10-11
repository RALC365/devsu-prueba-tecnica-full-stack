import { ApiError, UNEXPECTED_ERROR_MESSAGE, toApiError } from './api-error';

describe('toApiError', () => {
  it('devuelve el mismo ApiError', () => {
    const error = new ApiError(409, 'Ya existe un cliente con la identificación 1712345678');

    expect(toApiError(error)).toBe(error);
  });

  it('convierte otros errores en un ApiError genérico', () => {
    const error = toApiError(new TypeError('boom'));

    expect(error).toBeInstanceOf(ApiError);
    expect(error.message).toBe(UNEXPECTED_ERROR_MESSAGE);
    expect(error.fieldErrors).toEqual({});
  });
});
