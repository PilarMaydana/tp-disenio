'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { EvaluarCandidatosScreen } from '@/features/cu025-evaluar-candidatos';

/** CU 025 - Evaluar a Candidatos. */
export default function EvaluarCandidatosPage() {
  return (
    <DashboardLayout>
      <EvaluarCandidatosScreen />
    </DashboardLayout>
  );
}
