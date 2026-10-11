import { TipoCuenta } from './cuenta';
import { TipoMovimiento } from './movimiento';

export interface ReporteFiltro {
  clienteId: number;
  fechaInicio: string;
  fechaFin: string;
}

export interface ReporteCuenta {
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  saldoDisponible: number;
  estado: boolean;
  totalCreditos: number;
  totalDebitos: number;
}

export interface ReporteMovimiento {
  fecha: string;
  cliente: string;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
  tipoMovimiento: TipoMovimiento;
  movimiento: number;
  saldoDisponible: number;
}

export interface Reporte {
  clienteId: number;
  cliente: string;
  identificacion: string;
  fechaInicio: string;
  fechaFin: string;
  fechaGeneracion: string;
  totalCreditos: number;
  totalDebitos: number;
  cuentas: ReporteCuenta[];
  movimientos: ReporteMovimiento[];
}

export interface ReportePdf {
  nombreArchivo: string;
  tipoContenido: string;
  contenidoBase64: string;
}
