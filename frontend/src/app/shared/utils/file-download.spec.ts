import { base64ToBlob, downloadBlob } from './file-download';

describe('base64ToBlob', () => {
  it('convierte base64 a Blob', async () => {
    const blob = base64ToBlob(btoa('%PDF-1.7 contenido'), 'application/pdf');

    expect(blob.type).toBe('application/pdf');
    expect(blob.size).toBe('%PDF-1.7 contenido'.length);
    expect(await blob.text()).toBe('%PDF-1.7 contenido');
  });
});

describe('downloadBlob', () => {
  const createObjectURL = jest.fn(() => 'blob:http://localhost/reporte');
  const revokeObjectURL = jest.fn();

  beforeEach(() => {
    jest.useFakeTimers();
    Object.assign(URL, { createObjectURL, revokeObjectURL });
  });

  afterEach(() => jest.useRealTimers());

  it('descarga el blob con un link temporal', () => {
    const blob = new Blob(['%PDF'], { type: 'application/pdf' });
    const clickedLinks: HTMLAnchorElement[] = [];
    jest.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (
      this: HTMLAnchorElement,
    ) {
      clickedLinks.push(this);
    });

    downloadBlob(blob, 'estado-cuenta.pdf', document);

    const [clickedLink] = clickedLinks;
    expect(createObjectURL).toHaveBeenCalledWith(blob);
    expect(clickedLink.href).toBe('blob:http://localhost/reporte');
    expect(clickedLink.download).toBe('estado-cuenta.pdf');
    expect(clickedLink.isConnected).toBe(false);

    expect(revokeObjectURL).not.toHaveBeenCalled();
    jest.runAllTimers();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:http://localhost/reporte');
  });
});
