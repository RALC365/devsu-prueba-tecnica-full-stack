import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { TIPO_CUENTA_LABELS, TIPO_MOVIMIENTO_LABELS } from '../../../core/models';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { Icon } from '../../../shared/components/icon/icon';
import { Modal } from '../../../shared/components/modal/modal';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { QuickSearch } from '../../../shared/components/quick-search/quick-search';
import { TablePanel } from '../../../shared/components/table-panel/table-panel';
import { Flash } from '../../../shared/directives/flash';
import { MoneyPipe } from '../../../shared/pipes/money-pipe';
import { MovimientoForm } from '../movimiento-form/movimiento-form';
import { MovimientosStore } from '../movimientos-store';

@Component({
  selector: 'app-movimientos-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ConfirmDialog,
    DatePipe,
    Flash,
    Icon,
    Modal,
    MoneyPipe,
    MovimientoForm,
    PageHeader,
    QuickSearch,
    TablePanel,
  ],
  providers: [MovimientosStore],
  host: { class: 'page' },
  templateUrl: './movimientos-page.html',
})
export class MovimientosPage {
  protected readonly store = inject(MovimientosStore);
  protected readonly tipoCuentaLabels = TIPO_CUENTA_LABELS;
  protected readonly tipoMovimientoLabels = TIPO_MOVIMIENTO_LABELS;
  protected readonly creditos = computed(
    () => this.store.items().filter((movimiento) => movimiento.tipoMovimiento === 'CREDITO').length,
  );
  protected readonly debitos = computed(() => this.store.items().length - this.creditos());
}
