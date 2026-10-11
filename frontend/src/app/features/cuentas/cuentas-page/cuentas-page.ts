import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { ESTADO_CUENTA, TIPO_CUENTA_LABELS } from '../../../core/models';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { Icon } from '../../../shared/components/icon/icon';
import { Modal } from '../../../shared/components/modal/modal';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { QuickSearch } from '../../../shared/components/quick-search/quick-search';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { TablePanel } from '../../../shared/components/table-panel/table-panel';
import { Flash } from '../../../shared/directives/flash';
import { MoneyPipe } from '../../../shared/pipes/money-pipe';
import { CuentaForm } from '../cuenta-form/cuenta-form';
import { CuentasStore } from '../cuentas-store';

@Component({
  selector: 'app-cuentas-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ConfirmDialog,
    CuentaForm,
    Flash,
    Icon,
    Modal,
    MoneyPipe,
    PageHeader,
    QuickSearch,
    StatusBadge,
    TablePanel,
  ],
  providers: [CuentasStore],
  host: { class: 'page' },
  templateUrl: './cuentas-page.html',
})
export class CuentasPage {
  protected readonly store = inject(CuentasStore);
  protected readonly tipoCuentaLabels = TIPO_CUENTA_LABELS;
  protected readonly estadoCuenta = ESTADO_CUENTA;
  protected readonly saldoTotal = computed(() =>
    this.store.items().reduce((total, cuenta) => total + cuenta.saldoDisponible, 0),
  );
}
