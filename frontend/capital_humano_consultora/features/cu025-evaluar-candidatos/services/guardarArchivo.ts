/**
 * Guardado de archivos con diálogo "Guardar como" (CU 025 - pasos 15 a 17).
 *
 * Usa la File System Access API (Chrome / Edge), que abre el diálogo del sistema operativo para
 * elegir carpeta y nombre. En navegadores que no la soportan se usa la descarga estándar.
 */

interface SaveFilePickerOptions {
  suggestedName?: string;
  types?: { description: string; accept: Record<string, string[]> }[];
}

interface WritableFileStream {
  write(data: Blob): Promise<void>;
  close(): Promise<void>;
}

interface FileHandle {
  createWritable(): Promise<WritableFileStream>;
}

type VentanaConPicker = Window & {
  showSaveFilePicker?: (options?: SaveFilePickerOptions) => Promise<FileHandle>;
};

/** Resultado del guardado: el actor guardó el archivo o presionó "Cancelar" (flujo 16.A). */
export type ResultadoGuardado = 'guardado' | 'cancelado';

export async function guardarArchivoComo(contenido: Blob, nombreSugerido: string): Promise<ResultadoGuardado> {
  const ventana = window as VentanaConPicker;

  if (typeof ventana.showSaveFilePicker === 'function') {
    try {
      const handle = await ventana.showSaveFilePicker({
        suggestedName: nombreSugerido,
        types: [
          {
            description: 'Libro de Excel 97-2003',
            accept: { 'application/vnd.ms-excel': ['.xls'] },
          },
        ],
      });
      const writable = await handle.createWritable();
      await writable.write(contenido);
      await writable.close();
      return 'guardado';
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        return 'cancelado';
      }
      throw error;
    }
  }

  // Alternativa: descarga estándar del navegador
  const url = URL.createObjectURL(contenido);
  const enlace = document.createElement('a');
  enlace.href = url;
  enlace.download = nombreSugerido;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  URL.revokeObjectURL(url);
  return 'guardado';
}
