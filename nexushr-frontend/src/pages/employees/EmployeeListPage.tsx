import { useEffect, useState } from 'react';
import { Plus, Search, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import { employeeApi } from '../../api/employeeApi';
import { Table } from '../../components/ui/Table';
import { Pagination } from '../../components/ui/Pagination';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import type { Employee, EmployeeStatus } from '../../types/employee';
import { useAuth } from '../../hooks/useAuth';

const statusColor: Record<EmployeeStatus, 'green' | 'gray' | 'yellow' | 'red'> = {
  ACTIVE: 'green',
  INACTIVE: 'gray',
  ON_LEAVE: 'yellow',
  TERMINATED: 'red',
};

export function EmployeeListPage() {
  const { hasRole, hasAnyRole } = useAuth();
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const fetchEmployees = () => {
    setLoading(true);
    employeeApi.list({ search: search || undefined, page, size: 10 })
      .then((r) => {
        setEmployees(r.data.content);
        setTotalPages(r.data.totalPages);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchEmployees(); }, [page]);

  const handleDelete = async (id: string) => {
    try {
      await employeeApi.delete(id);
      setDeleteId(null);
      fetchEmployees();
    } catch {
      alert('Failed to delete employee');
      setDeleteId(null);
    }
  };

  const canDelete = hasAnyRole('ADMIN', 'MANAGER');

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Employees</h1>
          <p className="text-gray-500 mt-1">Manage your workforce</p>
        </div>
        {hasRole('ADMIN') && (
          <Link to="/employees/new">
            <Button><Plus className="w-4 h-4" /> Add Employee</Button>
          </Link>
        )}
      </div>

      <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
        <div className="flex gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && (setPage(0), fetchEmployees())}
              placeholder="Search by name, code, or email…"
              className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <Button variant="secondary" onClick={() => { setPage(0); fetchEmployees(); }}>Search</Button>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-2">
        {loading ? (
          <div className="h-48 flex items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
          </div>
        ) : (
          <>
            <Table
              keyField={(e) => e.id}
              data={employees}
              columns={[
                { header: 'Code', accessor: (e) => <span className="font-mono text-xs">{e.employeeCode}</span> },
                { header: 'Name', accessor: (e) => (
                    <Link to={`/employees/${e.id}`} className="font-medium text-gray-900 hover:text-blue-600">
                      {e.fullName}
                    </Link>
                  ) },
                { header: 'Department', accessor: (e) => e.department.name },
                { header: 'Designation', accessor: (e) => e.designation.title },
                { header: 'Status', accessor: (e) => <Badge color={statusColor[e.status]}>{e.status}</Badge> },
                ...(canDelete ? [{
                  header: 'Action',
                  accessor: (e: Employee) => (
                    <button
                      onClick={() => setDeleteId(e.id)}
                      className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Delete employee"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  ),
                }] : []),
              ]}
            />
            <div className="px-4 pb-2">
              <Pagination page={page} totalPages={totalPages} onChange={setPage} />
            </div>
          </>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      {deleteId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-2xl p-6 max-w-sm w-full mx-4">
            <div className="flex items-center justify-center w-12 h-12 bg-red-100 rounded-full mx-auto mb-4">
              <Trash2 className="w-6 h-6 text-red-600" />
            </div>
            <h3 className="text-lg font-bold text-gray-900 text-center">Permanently Delete Employee</h3>
            <p className="text-sm text-gray-500 text-center mt-2">
              This will <span className="font-semibold text-red-600">permanently remove</span> this employee and all their data (payroll, attendance, leaves, reviews). This action cannot be undone.
            </p>
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setDeleteId(null)}
                className="flex-1 px-4 py-2.5 border border-gray-300 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDelete(deleteId)}
                className="flex-1 px-4 py-2.5 bg-red-600 rounded-xl text-sm font-medium text-white hover:bg-red-700 transition"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}