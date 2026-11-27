'use client';

import React, { forwardRef } from 'react';
import { Icon, IconName } from '../Icon';

export type ButtonVariant = 'primary' | 'secondary' | 'secondary-soft' | 'icon' | 'outline' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  leftIcon?: IconName | string;
  rightIcon?: IconName | string;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      children,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      leftIcon,
      rightIcon,
      disabled,
      className = '',
      ...props
    },
    ref
  ) => {
    const baseStyles =
      'inline-flex items-center justify-center font-medium transition-all duration-200 cursor-pointer focus:outline-none select-none disabled:opacity-50 disabled:cursor-not-allowed disabled:pointer-events-none';

    const sizeStyles: Record<ButtonSize, string> = {
      sm: 'text-sm h-10 px-4 min-w-[110px] rounded-lg gap-1.5 whitespace-nowrap',
      md: 'text-base h-[46px] px-5 min-w-[120px] sm:min-w-[150px] rounded-lg gap-2 whitespace-nowrap',
      lg: 'text-lg h-12 px-8 rounded-xl gap-3 font-semibold whitespace-nowrap',
    };

    const variantStyles: Record<ButtonVariant, string> = {
      primary:
        'bg-[#3F2581] hover:bg-[#4E309D] active:bg-[#341E6C] text-white shadow-sm hover:shadow-md border-2 border-transparent',
      secondary:
        'bg-transparent hover:bg-[#F4F0FF] active:bg-[#EADDFF] text-[#3F2581] border-2 border-[#3F2581]',
      'secondary-soft':
        'bg-[#CDCEE6] hover:bg-[#B8B9DA] active:bg-[#A6A8CD] text-[#2D1B60] border-2 border-[#BDBEE0]',
      icon:
        'p-2.5 rounded-lg bg-transparent hover:bg-white/10 active:bg-white/20 text-white border border-white/20',
      outline:
        'bg-transparent hover:bg-[#F4F0FF] active:bg-[#EADDFF] text-[#3F2581] border-2 border-[#3F2581]',
      danger:
        'bg-[#D32F2F] hover:bg-[#B71C1C] active:bg-[#9A0007] text-white border-2 border-transparent',
    };

    return (
      <button
        ref={ref}
        className={`${baseStyles} ${variant === 'icon' ? '' : sizeStyles[size]} ${variantStyles[variant]} ${className}`}
        disabled={disabled || isLoading}
        {...props}
      >
        {isLoading ? (
          <span className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin mr-2" />
        ) : (
          leftIcon && <Icon name={leftIcon} size={size === 'sm' ? 16 : size === 'lg' ? 22 : 18} />
        )}
        {children}
        {!isLoading && rightIcon && (
          <Icon name={rightIcon} size={size === 'sm' ? 16 : size === 'lg' ? 22 : 18} />
        )}
      </button>
    );
  }
);

Button.displayName = 'Button';

export default Button;
