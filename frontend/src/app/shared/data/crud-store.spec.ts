import { ApplicationRef, Injectable } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';

import { ApiError } from '../../core/http/api-error';
import { ToastService } from '../../core/notifications/toast-service';
import { CrudApi, CrudStore } from './crud-store';

interface Item {
  id: number;
  nombre: string;
}

type ItemRequest = Omit<Item, 'id'>;

const ITEMS: Item[] = [
  { id: 1, nombre: 'Cuenta de ahorros' },
  { id: 2, nombre: 'Cuenta corriente' },
];

const api = {
  listar: jest.fn<Observable<Item[]>, []>(),
  crear: jest.fn<Observable<Item>, [ItemRequest]>(),
  actualizar: jest.fn<Observable<Item>, [number, ItemRequest]>(),
  eliminar: jest.fn<Observable<void>, [number]>(),
} satisfies CrudApi<Item, ItemRequest>;

@Injectable()
class ItemsStore extends CrudStore<Item, ItemRequest> {
  constructor() {
    super(api, {
      idOf: (item) => item.id,
      searchableValues: (item) => [item.nombre],
      messages: {
        created: 'Creado',
        updated: 'Actualizado',
        deleted: 'Eliminado',
        saveFailed: 'No se pudo guardar',
      },
    });
  }
}

describe('CrudStore', () => {
  const toast = { success: jest.fn(), error: jest.fn() };
  let store: ItemsStore;

  async function settle(): Promise<void> {
    await TestBed.inject(ApplicationRef).whenStable();
  }

  beforeEach(async () => {
    api.listar.mockReturnValue(of(ITEMS));
    TestBed.configureTestingModule({
      providers: [ItemsStore, { provide: ToastService, useValue: toast }],
    });
    store = TestBed.inject(ItemsStore);
    await settle();
  });

  it('carga y filtra el listado', () => {
    expect(store.items()).toEqual(ITEMS);
    expect(store.loading()).toBe(false);

    store.query.set('CORRIENTE');

    expect(store.visibleItems()).toEqual([ITEMS[1]]);
  });

  it('expone el error de carga y se recupera con reload', async () => {
    api.listar.mockReturnValue(
      throwError(() => new ApiError(0, 'No se pudo conectar con el servidor')),
    );
    store.reload();
    await settle();

    expect(store.loadError()).toBe('No se pudo conectar con el servidor');
    expect(store.items()).toEqual([]);

    api.listar.mockReturnValue(of(ITEMS));
    store.reload();
    await settle();

    expect(store.loadError()).toBeNull();
    expect(store.items()).toEqual(ITEMS);
  });

  it('abre y cierra el editor', () => {
    store.openEdit(ITEMS[0]);
    expect(store.editorOpen()).toBe(true);
    expect(store.editing()).toBe(ITEMS[0]);

    store.openCreate();
    expect(store.editing()).toBeNull();

    store.closeEditor();
    expect(store.editorOpen()).toBe(false);
  });

  it('crea, avisa y recarga', async () => {
    api.crear.mockReturnValue(of({ id: 3, nombre: 'Nueva' }));
    store.openCreate();

    store.save({ nombre: 'Nueva' });
    await settle();

    expect(api.crear).toHaveBeenCalledWith({ nombre: 'Nueva' });
    expect(toast.success).toHaveBeenCalledWith('Creado');
    expect(store.editorOpen()).toBe(false);
    expect(store.saving()).toBe(false);
    expect(api.listar).toHaveBeenCalledTimes(2);
  });

  it('actualiza usando el id', () => {
    api.actualizar.mockReturnValue(of({ id: 2, nombre: 'Editada' }));
    store.openEdit(ITEMS[1]);

    store.save({ nombre: 'Editada' });

    expect(api.actualizar).toHaveBeenCalledWith(2, { nombre: 'Editada' });
    expect(toast.success).toHaveBeenCalledWith('Actualizado');
  });

  it('si falla el guardado deja el editor abierto', () => {
    const error = new ApiError(422, 'Saldo no disponible', 'SALDO_NO_DISPONIBLE');
    api.crear.mockReturnValue(throwError(() => error));
    store.openCreate();

    store.save({ nombre: 'Nueva' });

    expect(store.saveError()).toBe(error);
    expect(store.editorOpen()).toBe(true);
    expect(store.saving()).toBe(false);
    expect(toast.error).toHaveBeenCalledWith('No se pudo guardar');
  });

  it('elimina y recarga', async () => {
    api.eliminar.mockReturnValue(of(undefined));
    store.requestDelete(ITEMS[0]);

    store.confirmDelete();
    await settle();

    expect(api.eliminar).toHaveBeenCalledWith(1);
    expect(toast.success).toHaveBeenCalledWith('Eliminado');
    expect(store.pendingDelete()).toBeNull();
    expect(store.deleting()).toBe(false);
    expect(api.listar).toHaveBeenCalledTimes(2);
  });

  it('muestra el error del backend al eliminar', () => {
    api.eliminar.mockReturnValue(
      throwError(
        () => new ApiError(409, 'No se puede eliminar la cuenta porque tiene movimientos'),
      ),
    );
    store.requestDelete(ITEMS[0]);

    store.confirmDelete();

    expect(toast.error).toHaveBeenCalledWith(
      'No se puede eliminar la cuenta porque tiene movimientos',
    );
    expect(store.pendingDelete()).toBeNull();
    expect(api.listar).toHaveBeenCalledTimes(1);
  });

  it('no elimina si se canceló', () => {
    store.requestDelete(ITEMS[0]);
    store.cancelDelete();

    store.confirmDelete();

    expect(store.pendingDelete()).toBeNull();
    expect(api.eliminar).not.toHaveBeenCalled();
  });
});
