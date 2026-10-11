import { Injectable, inject } from '@angular/core';

import { ClientesApi } from '../../core/api/clientes-api';
import {
  Cliente,
  ClienteRequest,
  ESTADO_CLIENTE,
  GENERO_LABELS,
  estadoLabel,
} from '../../core/models';
import { CrudStore } from '../../shared/data/crud-store';

@Injectable()
export class ClientesStore extends CrudStore<Cliente, ClienteRequest> {
  constructor() {
    super(inject(ClientesApi), {
      idOf: (cliente) => cliente.clienteId,
      searchableValues: (cliente) => [
        cliente.nombre,
        cliente.identificacion,
        GENERO_LABELS[cliente.genero],
        cliente.edad,
        cliente.direccion,
        cliente.telefono,
        estadoLabel(cliente.estado, ESTADO_CLIENTE),
      ],
      messages: {
        created: 'Cliente creado',
        updated: 'Cliente actualizado',
        deleted: 'Cliente eliminado',
        saveFailed: 'No se pudo guardar el cliente',
      },
    });
  }
}
