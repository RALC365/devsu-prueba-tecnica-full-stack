export type FieldErrors = Readonly<Record<string, string>>;

export class ApiError extends Error {
  override readonly name = 'ApiError';

  constructor(
    readonly status: number,
    message: string,
    readonly codigo?: string,
    readonly fieldErrors: FieldErrors = {},
  ) {
    super(message);
  }
}

export const UNEXPECTED_ERROR_MESSAGE = 'Ocurrió un error inesperado';

export function toApiError(error: unknown): ApiError {
  return error instanceof ApiError ? error : new ApiError(0, UNEXPECTED_ERROR_MESSAGE);
}
