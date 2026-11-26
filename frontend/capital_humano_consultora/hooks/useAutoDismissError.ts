'use client';

import { useEffect, useRef, useState } from 'react';

const TIEMPO_VIGENCIA_ERROR_MS = 10 * 60 * 1000;

export function useAutoDismissError(
  messageKey: string | null,
  onExpire?: () => void,
): boolean {
  const [dismissedKey, setDismissedKey] = useState<string | null>(null);
  const onExpireRef = useRef(onExpire);

  useEffect(() => {
    onExpireRef.current = onExpire;
  }, [onExpire]);

  useEffect(() => {
    if (!messageKey) {
      const resetTimeoutId = window.setTimeout(() => setDismissedKey(null), 0);
      return () => window.clearTimeout(resetTimeoutId);
    }

    const key = messageKey;
    const timeoutId = window.setTimeout(() => {
      setDismissedKey(key);
      onExpireRef.current?.();
    }, TIEMPO_VIGENCIA_ERROR_MS);

    return () => window.clearTimeout(timeoutId);
  }, [messageKey]);

  return Boolean(messageKey) && dismissedKey !== messageKey;
}
