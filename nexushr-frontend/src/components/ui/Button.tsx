import { clsx } from 'clsx';
import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  loading?: boolean;
}

export function Button({
  variant = 'primary',
  loading,
  disabled,
  className,
  children,
  ...props
}: ButtonProps) {
  const variants = {
    primary:   'bg-blue-600 hover:bg-blue-700 text-white',
    secondary: 'bg-gray-100 hover:bg-gray-200 text-gray-700',
    danger:    'bg-red-600 hover:bg-red-700 text-white',
    ghost:     'bg-transparent hover:bg-gray-100 text-gray-700',
  };

  return (
    <button
      className={clsx(
        'inline-flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl',
        'text-sm font-semibold transition disabled:opacity-50 disabled:cursor-not-allowed',
        variants[variant],
        className
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <span className="h-4 w-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
      )}
      {children}
    </button>
  );
}