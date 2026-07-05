import { clsx } from 'clsx';
import React from 'react';

export function Card({ className, children, ...props }:
  React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={clsx(
        'bg-white rounded-2xl shadow-sm border border-gray-100 p-6',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
}