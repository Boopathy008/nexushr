import { useEffect, useState } from 'react';
import { employeeApi } from '../api/employeeApi';
import { useAuth } from './useAuth';

export function useEmployeeProfile() {
  const { user } = useAuth();
  const [employeeId, setEmployeeId] = useState<string | null>(
    localStorage.getItem('employeeId')
  );
  const [employeeName, setEmployeeName] = useState<string | null>(
    localStorage.getItem('employeeName')
  );
  const [loading, setLoading] = useState(!employeeId);

  useEffect(() => {
    if (employeeId || !user?.userId) {
      setLoading(false);
      return;
    }

    employeeApi
      .getByUserId(user.userId)
      .then((res) => {
        const emp = res.data;
        if (emp) {
          localStorage.setItem('employeeId', emp.id);
          localStorage.setItem('employeeName', emp.fullName);
          setEmployeeId(emp.id);
          setEmployeeName(emp.fullName);
        }
      })
      .catch(() => {
        // User may not have a linked employee profile (e.g. pure admin)
      })
      .finally(() => setLoading(false));
  }, [user, employeeId]);

  return { employeeId, employeeName, loading };
}