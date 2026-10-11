export interface EstadoLabels {
  readonly activo: string;
  readonly inactivo: string;
}

export const ESTADO_CLIENTE: EstadoLabels = { activo: 'Activo', inactivo: 'Inactivo' };

export const ESTADO_CUENTA: EstadoLabels = { activo: 'Activa', inactivo: 'Inactiva' };

export function estadoLabel(estado: boolean, labels: EstadoLabels): string {
  return estado ? labels.activo : labels.inactivo;
}
