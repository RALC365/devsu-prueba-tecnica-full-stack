export type SearchableValue = string | number | null | undefined;

export function normalizeText(value: string): string {
  return value.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
}

export function filterBySearch<T>(
  items: readonly T[],
  query: string,
  searchableValues: (item: T) => readonly SearchableValue[],
): readonly T[] {
  const terms = normalizeText(query).split(/\s+/).filter(Boolean);
  if (terms.length === 0) {
    return items;
  }
  return items.filter((item) => {
    // se une con \n para que un término no coincida entre dos columnas
    const text = normalizeText(
      searchableValues(item)
        .filter((value) => value != null)
        .join('\n'),
    );
    return terms.every((term) => text.includes(term));
  });
}
