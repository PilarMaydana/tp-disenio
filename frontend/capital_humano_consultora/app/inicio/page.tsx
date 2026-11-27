'use client';

import React from 'react';
import { DashboardLayout } from '@/components/layouts/DashboardLayout';
import { InicioConsultorScreen } from '@/features/inicio-consultor';

export default function InicioConsultorPage() {
  return (
    <DashboardLayout>
      <InicioConsultorScreen />
    </DashboardLayout>
  );
}
