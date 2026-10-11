// sin matchMedia (pruebas, SSR) se asume que no hay que animar
export function canAnimate(doc: Document): boolean {
  const view = doc.defaultView;
  return (
    typeof view?.matchMedia === 'function' &&
    typeof view.requestAnimationFrame === 'function' &&
    !view.matchMedia('(prefers-reduced-motion: reduce)').matches
  );
}
