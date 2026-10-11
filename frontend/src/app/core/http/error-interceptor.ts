import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { ApiError, FieldErrors } from './api-error';

export const CONNECTION_ERROR_MESSAGE = 'No se pudo conectar con el servidor';
export const SERVER_ERROR_MESSAGE = 'Error en el servidor, intente más tarde';
export const REQUEST_ERROR_MESSAGE = 'No se pudo completar la solicitud';

interface ProblemDetail {
  readonly title?: string;
  readonly detail?: string;
  readonly codigo?: string;
  readonly errores: FieldErrors;
}

export const errorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((error: unknown) =>
      throwError(() => (error instanceof HttpErrorResponse ? toApiError(error) : error)),
    ),
  );

function toApiError(response: HttpErrorResponse): ApiError {
  if (response.status === 0) {
    return new ApiError(0, CONNECTION_ERROR_MESSAGE);
  }
  const problem = parseProblemDetail(response.error);
  const fallback =
    response.status >= 500 ? SERVER_ERROR_MESSAGE : (problem.title ?? REQUEST_ERROR_MESSAGE);
  return new ApiError(response.status, problem.detail ?? fallback, problem.codigo, problem.errores);
}

function parseProblemDetail(body: unknown): ProblemDetail {
  if (!isRecord(body)) {
    return { errores: {} };
  }
  return {
    title: nonEmptyString(body['title']),
    detail: nonEmptyString(body['detail']),
    codigo: nonEmptyString(body['codigo']),
    errores: isRecord(body['errores']) ? stringEntries(body['errores']) : {},
  };
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function nonEmptyString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim() !== '' ? value : undefined;
}

function stringEntries(record: Record<string, unknown>): FieldErrors {
  return Object.fromEntries(
    Object.entries(record).filter(
      (entry): entry is [string, string] => typeof entry[1] === 'string',
    ),
  );
}
