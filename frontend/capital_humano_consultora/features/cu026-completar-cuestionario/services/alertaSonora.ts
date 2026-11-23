/**
 * Emite una alerta sonora suave (CU 026 - mensajes 18, 24 y 50 del diagrama de secuencia).
 * Si el navegador no permite reproducir audio, no hace nada.
 */
export function emitirAlertaSonora(): void {
  if (typeof window === 'undefined') return;
  const Contexto =
    window.AudioContext || (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
  if (!Contexto) return;
  try {
    const ctx = new Contexto();
    const oscilador = ctx.createOscillator();
    const volumen = ctx.createGain();
    oscilador.type = 'sine';
    oscilador.frequency.value = 660;
    volumen.gain.setValueAtTime(0.0001, ctx.currentTime);
    volumen.gain.exponentialRampToValueAtTime(0.15, ctx.currentTime + 0.02);
    volumen.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.35);
    oscilador.connect(volumen).connect(ctx.destination);
    oscilador.start();
    oscilador.stop(ctx.currentTime + 0.4);
    oscilador.onended = () => ctx.close();
  } catch {
    /* sin audio disponible */
  }
}
