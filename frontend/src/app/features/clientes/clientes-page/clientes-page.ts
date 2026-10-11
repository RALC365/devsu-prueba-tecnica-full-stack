import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { GENERO_LABELS } from '../../../core/models';
import { Avatar } from '../../../shared/components/avatar/avatar';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { Icon } from '../../../shared/components/icon/icon';
import { Modal } from '../../../shared/components/modal/modal';
import { PageHeader } from '../../../shared/components/page-header/page-header';
import { QuickSearch } from '../../../shared/components/quick-search/quick-search';
import { StatusBadge } from '../../../shared/components/status-badge/status-badge';
import { TablePanel } from '../../../shared/components/table-panel/table-panel';
import { Flash } from '../../../shared/directives/flash';
import { ClienteForm } from '../cliente-form/cliente-form';
import { ClientesStore } from '../clientes-store';

@Component({
  selector: 'app-clientes-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    Avatar,
    ClienteForm,
    ConfirmDialog,
    Flash,
    Icon,
    Modal,
    PageHeader,
    QuickSearch,
    StatusBadge,
    TablePanel,
  ],
  providers: [ClientesStore],
  host: { class: 'page' },
  templateUrl: './clientes-page.html',
})
export class ClientesPage {
  protected readonly store = inject(ClientesStore);
  protected readonly generoLabels = GENERO_LABELS;
  protected readonly activos = computed(
    () => this.store.items().filter((cliente) => cliente.estado).length,
  );
}
