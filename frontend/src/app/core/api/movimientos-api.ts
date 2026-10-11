import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Movimiento, MovimientoRequest } from '../models';
import { API_BASE_URL } from './api-base-url';

@Injectable({ providedIn: 'root' })
export class MovimientosApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(API_BASE_URL)}/movimientos`;

  listar(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(this.url);
  }

  crear(request: MovimientoRequest): Observable<Movimiento> {
    return this.http.post<Movimiento>(this.url, request);
  }

  actualizar(movimientoId: number, request: MovimientoRequest): Observable<Movimiento> {
    return this.http.put<Movimiento>(`${this.url}/${movimientoId}`, request);
  }

  eliminar(movimientoId: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${movimientoId}`);
  }
}
