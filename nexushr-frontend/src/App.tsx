import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/guards/ProtectedRoute';
import { RoleGuard } from './components/guards/RoleGuard';
import { AppLayout } from './components/layout/AppLayout';

import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { DashboardRouter } from './pages/dashboard/DashboardRouter';
import { EmployeeListPage } from './pages/employees/EmployeeListPage';
import { EmployeeDetailPage } from './pages/employees/EmployeeDetailPage';
import { EmployeeFormPage } from './pages/employees/EmployeeFormPage';
import { AttendancePage } from './pages/attendance/AttendancePage';
import { MonthlyReportPage } from './pages/attendance/MonthlyReportPage';
import { LeaveListPage } from './pages/leave/LeaveListPage';
import { ApplyLeavePage } from './pages/leave/ApplyLeavePage';
import { LeaveBalancePage } from './pages/leave/LeaveBalancePage';
import { PayslipPage } from './pages/payroll/PayslipPage';
import { PayrollReportPage } from './pages/payroll/PayrollReportPage';
import { PerformancePage } from './pages/performance/PerformancePage';
import { IntelligenceDashboard } from './pages/intelligence/IntelligenceDashboard';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardRouter />} />

              <Route path="/employees" element={
                <RoleGuard allowedRoles={['ADMIN', 'MANAGER']}><EmployeeListPage /></RoleGuard>
              } />
              <Route path="/employees/new" element={
                <RoleGuard allowedRoles={['ADMIN']}><EmployeeFormPage /></RoleGuard>
              } />
              <Route path="/employees/:id" element={<EmployeeDetailPage />} />
              <Route path="/employees/:id/edit" element={
                <RoleGuard allowedRoles={['ADMIN', 'MANAGER']}><EmployeeFormPage /></RoleGuard>
              } />

              <Route path="/attendance" element={<AttendancePage />} />
              <Route path="/attendance/report" element={<MonthlyReportPage />} />

              <Route path="/leaves" element={<LeaveListPage />} />
              <Route path="/leaves/apply" element={<ApplyLeavePage />} />
              <Route path="/leaves/balance" element={<LeaveBalancePage />} />

              <Route path="/payroll" element={<PayslipPage />} />
              <Route path="/payroll/reports" element={
                <RoleGuard allowedRoles={['ADMIN']}><PayrollReportPage /></RoleGuard>
              } />

              <Route path="/performance" element={<PerformancePage />} />

              <Route path="/intelligence" element={
                <RoleGuard allowedRoles={['ADMIN', 'MANAGER']}><IntelligenceDashboard /></RoleGuard>
              } />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
