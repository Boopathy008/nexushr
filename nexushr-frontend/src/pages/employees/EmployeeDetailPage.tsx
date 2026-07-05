import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Mail, Phone, Calendar, Building2, Briefcase, Edit } from 'lucide-react';
import { employeeApi } from '../../api/employeeApi';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import type { Employee, EmployeeStatus } from '../../types/employee';
import { useAuth } from '../../hooks/useAuth';

const statusColor: Record<EmployeeStatus, 'green' | 'gray' | 'yellow' | 'red'> = {
  ACTIVE: 'green', INACTIVE: 'gray', ON_LEAVE: 'yellow', TERMINATED: 'red',
};

export function EmployeeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { hasAnyRole } = useAuth();
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    employeeApi.getById(id).then((r) => setEmployee(r.data)).finally(() => setLoading(false));
  }, [id]);

  if (loading) return (
    <div className="flex h-64 items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
    </div>
  );

  if (!employee) return <p className="text-gray-500">Employee not found.</p>;

  return (
    <div className="space-y-6 max-w-3xl">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 bg-blue-100 rounded-2xl flex items-center justify-center text-blue-700 text-xl font-bold">
            {employee.firstName[0]}{employee.lastName[0]}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{employee.fullName}</h1>
            <p className="text-gray-500">{employee.designation.title} · {employee.employeeCode}</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <Badge color={statusColor[employee.status]}>{employee.status}</Badge>
          {hasAnyRole('ADMIN', 'MANAGER') && (
            <Link to={`/employees/${employee.id}/edit`}>
              <Button variant="secondary"><Edit className="w-4 h-4" /> Edit</Button>
            </Link>
          )}
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 grid grid-cols-1 sm:grid-cols-2 gap-6">
        <div className="flex items-start gap-3">
          <Mail className="w-5 h-5 text-gray-400 mt-0.5" />
          <div>
            <p className="text-xs text-gray-400">Email</p>
            <p className="text-sm font-medium text-gray-900">{employee.email}</p>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <Phone className="w-5 h-5 text-gray-400 mt-0.5" />
          <div>
            <p className="text-xs text-gray-400">Phone</p>
            <p className="text-sm font-medium text-gray-900">{employee.phone || '—'}</p>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <Building2 className="w-5 h-5 text-gray-400 mt-0.5" />
          <div>
            <p className="text-xs text-gray-400">Department</p>
            <p className="text-sm font-medium text-gray-900">{employee.department.name}</p>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <Briefcase className="w-5 h-5 text-gray-400 mt-0.5" />
          <div>
            <p className="text-xs text-gray-400">Designation</p>
            <p className="text-sm font-medium text-gray-900">{employee.designation.title} ({employee.designation.grade})</p>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <Calendar className="w-5 h-5 text-gray-400 mt-0.5" />
          <div>
            <p className="text-xs text-gray-400">Date of Joining</p>
            <p className="text-sm font-medium text-gray-900">{employee.dateOfJoining}</p>
          </div>
        </div>
        <div className="flex items-start gap-3">
          <Calendar className="w-5 h-5 text-gray-400 mt-0.5" />
          <div>
            <p className="text-xs text-gray-400">Date of Birth</p>
            <p className="text-sm font-medium text-gray-900">{employee.dateOfBirth || '—'}</p>
          </div>
        </div>
      </div>

      {employee.address && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <p className="text-xs text-gray-400 mb-1">Address</p>
          <p className="text-sm text-gray-900">{employee.address}</p>
        </div>
      )}
    </div>
  );
}