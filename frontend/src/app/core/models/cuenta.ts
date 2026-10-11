export const TIPOS_CUENTA = ['AHORROS', 'CORRIENTE'] as const;

export type TipoCuenta = (typeof TIPOS_CUENTA)[number];

export const TIPO_CUENTA_LABELS: Readonly<Record<TipoCuenta, string>> = {
  AHORROS: 'Ahorros',
  CORRIENTE: 'Corriente',
};

export interface Cuenta {
  cuentaId: number;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  saldoDisponible: number;
  estado: boolean;
  clienteId: number;
  cliente: string;
}

export interface CuentaRequest {
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
  clienteId: number;
}

export function cuentaLabel({ numeroCuenta, tipoCuenta, cliente }: Cuenta): string {
  return `${numeroCuenta} - ${TIPO_CUENTA_LABELS[tipoCuenta]} - ${cliente}`;
}
