import { Menu } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';

export function Topbar({ onToggle }: { onToggle: () => void }) {
  const { user } = useAuth();

  return (
    <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shrink-0">
      <button onClick={onToggle} className="text-gray-500 hover:text-gray-900">
        <Menu className="w-5 h-5" />
      </button>
      <div className="flex items-center gap-3">
        <div className="text-right">
          <p className="text-sm font-medium text-gray-900">{user?.username}</p>
          <p className="text-xs text-gray-400">{user?.role}</p>
        </div>
        <div className="w-9 h-9 bg-blue-100 rounded-full flex items-center justify-center text-blue-700 font-semibold text-sm">
          {user?.username?.charAt(0).toUpperCase()}
        </div>
      </div>
    </header>
  );
}