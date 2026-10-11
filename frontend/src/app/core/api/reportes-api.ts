import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Reporte, ReporteFiltro, ReportePdf } from '../models';
import { API_BASE_URL } from './api-base-url';

@Injectable({ providedIn: 'root' })
export class ReportesApi {
  private readonly http = inject(HttpClient);
  private readonly url = `${inject(API_BASE_URL)}/reportes`;

  generar(filtro: ReporteFiltro): Observable<Reporte> {
    return this.http.get<Reporte>(this.url, { params: toParams(filtro) });
  }

  descargarPdf(filtro: ReporteFiltro): Observable<ReportePdf> {
    return this.http.get<ReportePdf>(`${this.url}/pdf`, { params: toParams(filtro) });
  }
}

function toParams({ clienteId, fechaInicio, fechaFin }: ReporteFiltro): HttpParams {
  return new HttpParams({ fromObject: { clienteId, fechaInicio, fechaFin } });
}
