import { TipoCuenta } from './cuenta';

export const TIPOS_MOVIMIENTO = ['CREDITO', 'DEBITO'] as const;

export type TipoMovimiento = (typeof TIPOS_MOVIMIENTO)[number];

export const TIPO_MOVIMIENTO_LABELS: Readonly<Record<TipoMovimiento, string>> = {
  CREDITO: 'Crédito',
  DEBITO: 'Débito',
};

export interface Movimiento {
  movimientoId: number;
  fecha: string;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  tipoMovimiento: TipoMovimiento;
  valor: number;
  saldo: number;
  clienteId: number;
  cliente: string;
}

export interface MovimientoRequest {
  numeroCuenta: string;
  tipoMovimiento: TipoMovimiento;
  valor: number;
}
