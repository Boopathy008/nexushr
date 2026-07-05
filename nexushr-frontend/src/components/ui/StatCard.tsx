import React from 'react';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ElementType;
  color: string;       // bg-blue-500 etc.
  sub?: string;
  accent?: string;     // e.g. "border-blue-500"
}

export function StatCard({ title, value, icon: Icon, color, sub, accent }: StatCardProps) {
  return (
    <div
      className={`
        relative bg-white rounded-xl p-5
        shadow-sm border border-gray-100
        hover:shadow-md hover:-translate-y-0.5
        transition-all duration-200 overflow-hidden
        ${accent ? `border-b-4 ${accent}` : 'border-b-4 border-gray-200'}
      `}
    >
      {/* header row: label left, icon right */}
      <div className="flex items-start justify-between mb-3">
        <p className="text-xs font-semibold text-gray-500 tracking-wide uppercase leading-tight pr-2">
          {title}
        </p>
        <div className={`flex-shrink-0 p-2.5 rounded-xl ${color} shadow-md`}>
          <Icon className="w-4 h-4 text-white" strokeWidth={2} />
        </div>
      </div>

      {/* value */}
      <p className="text-2xl font-bold text-gray-900 leading-none truncate">{value}</p>

      {/* subtitle */}
      {sub && (
        <p className="text-xs text-gray-400 mt-1.5 font-medium">{sub}</p>
      )}
    </div>
  );
}