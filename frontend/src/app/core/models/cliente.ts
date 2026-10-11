export const GENEROS = ['MASCULINO', 'FEMENINO', 'OTRO'] as const;

export type Genero = (typeof GENEROS)[number];

export const GENERO_LABELS: Readonly<Record<Genero, string>> = {
  MASCULINO: 'Masculino',
  FEMENINO: 'Femenino',
  OTRO: 'Otro',
};

export interface Cliente {
  clienteId: number;
  nombre: string;
  genero: Genero;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono: string;
  estado: boolean;
}

export interface ClienteRequest {
  nombre: string;
  genero: Genero;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono: string;
  contrasena?: string;
  estado: boolean;
}

export function clienteLabel({ nombre, identificacion }: Cliente): string {
  return `${nombre} - ${identificacion}`;
}
