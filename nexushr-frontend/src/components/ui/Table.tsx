import React from 'react';

interface Column<T> {
  header: React.ReactNode;
  accessor: (item: T) => React.ReactNode;
}

interface TableProps<T> {
  data: T[];
  columns: Column<T>[];
  keyField: (item: T) => string | number;
  emptyMessage?: string;
}

export function Table<T>({
  data,
  columns,
  keyField,
  emptyMessage = 'No data available',
}: TableProps<T>) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-gray-100 bg-gray-50/50">
            {columns.map((column, index) => (
              <th
                key={index}
                className="px-6 py-3.5 text-xs font-semibold uppercase tracking-wider text-gray-500"
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-50">
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length}
                className="px-6 py-10 text-center text-sm text-gray-400"
              >
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((item) => (
              <tr key={keyField(item)} className="hover:bg-gray-50/50 transition-colors">
                {columns.map((column, index) => (
                  <td key={index} className="px-6 py-4 text-sm text-gray-600 whitespace-nowrap">
                    {column.accessor(item)}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
