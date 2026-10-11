import { DestroyRef, computed, inject, signal } from '@angular/core';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize } from 'rxjs';

import { ApiError, toApiError } from '../../core/http/api-error';
import { ToastService } from '../../core/notifications/toast-service';
import { Highlight } from '../directives/flash';
import { SearchableValue, filterBySearch } from '../utils/text-search';
import { toLookup } from './lookup';

export interface CrudApi<T, TRequest> {
  listar(): Observable<T[]>;
  crear(request: TRequest): Observable<T>;
  actualizar(id: number, request: TRequest): Observable<T>;
  eliminar(id: number): Observable<void>;
}

export interface CrudMessages {
  readonly created: string;
  readonly updated: string;
  readonly deleted: string;
  readonly saveFailed: string;
}

export interface CrudConfig<T> {
  readonly idOf: (item: T) => number;
  readonly searchableValues: (item: T) => readonly SearchableValue[];
  readonly messages: CrudMessages;
}

interface CrudState<T> {
  readonly editorOpen: boolean;
  readonly editing: T | null;
  readonly saving: boolean;
  readonly saveError: ApiError | null;
  readonly pendingDelete: T | null;
  readonly deleting: boolean;
}

export abstract class CrudStore<T, TRequest> {
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  // stream se llama después del constructor, cuando api ya está asignada
  private readonly list = rxResource({ stream: () => this.api.listar(), defaultValue: [] });
  private readonly listState = toLookup(this.list);
  private readonly state = signal<CrudState<T>>({
    editorOpen: false,
    editing: null,
    saving: false,
    saveError: null,
    pendingDelete: null,
    deleting: false,
  });

  readonly items = computed(() => this.listState().items);
  readonly loading = computed(() => this.listState().loading);
  readonly loadError = computed(() => this.listState().error);

  readonly query = signal('');
  readonly visibleItems = computed(() =>
    filterBySearch(this.items(), this.query(), this.config.searchableValues),
  );

  readonly editorOpen = computed(() => this.state().editorOpen);
  readonly editing = computed(() => this.state().editing);
  readonly saving = computed(() => this.state().saving);
  readonly saveError = computed(() => this.state().saveError);
  readonly pendingDelete = computed(() => this.state().pendingDelete);
  readonly deleting = computed(() => this.state().deleting);

  // el último registro guardado, para resaltar su fila
  private readonly lastSaved = signal<Highlight | null>(null);
  readonly highlighted = this.lastSaved.asReadonly();

  protected constructor(
    private readonly api: CrudApi<T, TRequest>,
    private readonly config: CrudConfig<T>,
  ) {}

  reload(): void {
    this.list.reload();
  }

  openCreate(): void {
    this.patch({ editorOpen: true, editing: null, saveError: null });
  }

  openEdit(item: T): void {
    this.patch({ editorOpen: true, editing: item, saveError: null });
  }

  closeEditor(): void {
    this.patch({ editorOpen: false, editing: null });
  }

  save(request: TRequest): void {
    const editing = this.editing();
    const request$ = editing
      ? this.api.actualizar(this.config.idOf(editing), request)
      : this.api.crear(request);

    this.patch({ saving: true, saveError: null });
    request$
      .pipe(
        finalize(() => this.patch({ saving: false })),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (saved) => {
          this.lastSaved.set({ id: this.config.idOf(saved) });
          this.toast.success(editing ? this.config.messages.updated : this.config.messages.created);
          this.closeEditor();
          this.reload();
        },
        error: (error: unknown) => {
          this.patch({ saveError: toApiError(error) });
          this.toast.error(this.config.messages.saveFailed);
        },
      });
  }

  requestDelete(item: T): void {
    this.patch({ pendingDelete: item });
  }

  cancelDelete(): void {
    this.patch({ pendingDelete: null });
  }

  confirmDelete(): void {
    const item = this.pendingDelete();
    if (!item) {
      return;
    }
    this.patch({ deleting: true });
    this.api
      .eliminar(this.config.idOf(item))
      .pipe(
        finalize(() => this.patch({ deleting: false, pendingDelete: null })),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.toast.success(this.config.messages.deleted);
          this.reload();
        },
        error: (error: unknown) => this.toast.error(toApiError(error).message),
      });
  }

  private patch(changes: Partial<CrudState<T>>): void {
    this.state.update((state) => ({ ...state, ...changes }));
  }
}
