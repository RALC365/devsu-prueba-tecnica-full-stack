import { SearchableValue, filterBySearch, normalizeText } from './text-search';

describe('normalizeText', () => {
  it('quita mayúsculas, tildes y espacios', () => {
    expect(normalizeText('  José PEÑA Ángel ')).toBe('jose pena angel');
  });
});

describe('filterBySearch', () => {
  interface Fila {
    nombre: string;
    ciudad: string | null;
    saldo: number;
  }

  const filas: Fila[] = [
    { nombre: 'José Lema', ciudad: 'Quito', saldo: 1425 },
    { nombre: 'Marianela Montalvo', ciudad: null, saldo: 700 },
    { nombre: 'Juan Osorio', ciudad: 'Otavalo', saldo: 150 },
  ];
  const values = (fila: Fila): SearchableValue[] => [fila.nombre, fila.ciudad, fila.saldo];
  const nombres = (result: readonly Fila[]) => result.map((fila) => fila.nombre);

  it('devuelve todo con una búsqueda vacía', () => {
    expect(filterBySearch(filas, '', values)).toBe(filas);
    expect(filterBySearch(filas, '   ', values)).toBe(filas);
  });

  it('ignora mayúsculas y tildes en cualquier columna', () => {
    expect(nombres(filterBySearch(filas, 'JOSE', values))).toEqual(['José Lema']);
    expect(nombres(filterBySearch(filas, 'otavalo', values))).toEqual(['Juan Osorio']);
    expect(nombres(filterBySearch(filas, '1425', values))).toEqual(['José Lema']);
  });

  it('exige todas las palabras', () => {
    expect(nombres(filterBySearch(filas, 'juan otavalo', values))).toEqual(['Juan Osorio']);
    expect(nombres(filterBySearch(filas, 'juan quito', values))).toEqual([]);
  });

  it('no junta columnas al buscar', () => {
    expect(filterBySearch(filas, 'lemaquito', values)).toEqual([]);
  });

  it('ignora valores null', () => {
    expect(nombres(filterBySearch(filas, 'null', values))).toEqual([]);
  });
});
