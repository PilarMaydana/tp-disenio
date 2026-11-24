'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { CompletarCuestionarioScreen } from '@/features/cu026-completar-cuestionario';

/** CU 026 - Completar cuestionario. */
export default function CandidatoCuestionarioPage() {
  return (
    <DashboardLayout>
      <CompletarCuestionarioScreen />
    </DashboardLayout>
  );
}
