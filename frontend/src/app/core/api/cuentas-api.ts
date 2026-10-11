import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Cuenta, CuentaRequest } from '../models';
import { API_BASE_URL } from './api-base-url';

@Injectable({ providedIn: 'root' })
export class CuentasApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(API_BASE_URL)}/cuentas`;

  listar(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(this.url);
  }

  crear(request: CuentaRequest): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.url, request);
  }

  actualizar(cuentaId: number, request: CuentaRequest): Observable<Cuenta> {
    return this.http.put<Cuenta>(`${this.url}/${cuentaId}`, request);
  }

  eliminar(cuentaId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${cuentaId}`);
  }
}
