'use client';

import React from 'react';
import { Icon } from '../Icon';

export interface CheckboxProps {
  id?: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  label?: React.ReactNode;
  disabled?: boolean;
  className?: string;
}

export const Checkbox: React.FC<CheckboxProps> = ({
  id,
  checked,
  onChange,
  label,
  disabled = false,
  className = '',
}) => {
  // Id único por instancia: el label puede ser un ReactNode (antes generaba "cb-[object-object]",
  // repetido entre checkboxes) y dos checkboxes con el mismo texto no deben compartir id.
  const autoId = React.useId();
  const generatedId = id ?? autoId;

  return (
    <label
      htmlFor={generatedId}
      className={`inline-flex items-center gap-3 cursor-pointer select-none ${
        disabled ? 'opacity-50 cursor-not-allowed' : ''
      } ${className}`}
    >
      <div className="relative flex items-center justify-center">
        <input
          id={generatedId}
          type="checkbox"
          checked={checked}
          disabled={disabled}
          onChange={(e) => onChange(e.target.checked)}
          className="sr-only peer"
        />
        <div
          className={`w-6 h-6 rounded-md border transition-all flex items-center justify-center peer-focus-visible:ring-2 peer-focus-visible:ring-[#3F2581] peer-focus-visible:ring-offset-1 ${
            checked
              ? 'bg-[#CDCEE6] border-[#3F2581] shadow-xs'
              : 'bg-white border-[#BDBEE0] hover:border-[#3F2581]'
          }`}
        >
          {checked && <Icon name="check" size={16} className="text-[#3F2581] stroke-[3]" />}
        </div>
      </div>
      {label && <span className="text-sm md:text-base font-normal text-[#000000]">{label}</span>}
    </label>
  );
};

export default Checkbox;
