import { DOCUMENT } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  linkedSignal,
  signal,
} from '@angular/core';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { ClientesApi } from '../../../core/api/clientes-api';
import { ReportesApi } from '../../../core/api/reportes-api';
import { toApiError } from '../../../core/http/api-error';
import { Reporte, ReporteFiltro } from '../../../core/models';
import { ToastService } from '../../../core/notifications/toast-service';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorAlert } from '../../../shared/components/error-alert/error-alert';
import { LoadingIndicator } from '../../../shared/components/loading-indicator/loading-indicator';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { toLookup } from '../../../shared/data/lookup';
import { base64ToBlob, downloadBlob } from '../../../shared/utils/file-download';
import { ReporteFilterForm } from '../reporte-filter-form/reporte-filter-form';
import { ReporteView } from '../reporte-view/reporte-view';

const PDF_CONTENT_TYPE = 'application/pdf';

@Component({
  selector: 'app-reportes-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'page' },
  imports: [EmptyState, ErrorAlert, LoadingIndicator, PageHeader, ReporteFilterForm, ReporteView],
  templateUrl: './reportes-page.html',
  styleUrl: './reportes-page.scss',
})
export class ReportesPage {
  private readonly clientesApi = inject(ClientesApi);
  private readonly reportesApi = inject(ReportesApi);
  private readonly toast = inject(ToastService);
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly clientes = toLookup(
    rxResource({ stream: () => this.clientesApi.listar(), defaultValue: [] }),
  );

  private readonly filtro = signal<ReporteFiltro | undefined>(undefined);

  protected readonly reporte = rxResource({
    params: () => this.filtro(),
    stream: ({ params }) => this.reportesApi.generar(params),
  });

  protected readonly reporteVisible = linkedSignal<Reporte | null, Reporte | null>({
    source: () => (this.reporte.hasValue() ? this.reporte.value() : null),
    computation: (actual, previous) =>
      actual ?? (this.reporte.isLoading() ? (previous?.value ?? null) : null),
  });

  protected readonly downloading = signal(false);

  protected generate(filtro: ReporteFiltro): void {
    this.filtro.set(filtro);
  }

  protected downloadPdf(filtro: ReporteFiltro): void {
    this.downloading.set(true);
    this.reportesApi
      .descargarPdf(filtro)
      .pipe(
        finalize(() => this.downloading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (pdf) => {
          downloadBlob(
            base64ToBlob(pdf.contenidoBase64, PDF_CONTENT_TYPE),
            pdf.nombreArchivo,
            this.document,
          );
          this.toast.success('PDF descargado');
        },
        error: (error: unknown) => this.toast.error(toApiError(error).message),
      });
  }
}
