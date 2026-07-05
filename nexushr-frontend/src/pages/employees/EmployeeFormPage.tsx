import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { employeeApi, departmentApi, designationApi } from '../../api/employeeApi';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import type { Department, Designation } from '../../types/employee';

interface FormData {
  username?: string;
  email: string;
  password?: string;
  firstName: string;
  lastName: string;
  phone?: string;
  gender?: string;
  dateOfBirth?: string;
  dateOfJoining: string;
  address?: string;
  departmentId: string;
  designationId: string;
}

export function EmployeeFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [departments, setDepartments] = useState<Department[]>([]);
  const [designations, setDesignations] = useState<Designation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { register, handleSubmit, watch, reset, formState: { errors } } = useForm<FormData>();
  const selectedDept = watch('departmentId');

  useEffect(() => {
    departmentApi.list().then((r) => setDepartments(r.data));
  }, []);

  useEffect(() => {
    if (selectedDept) {
      designationApi.byDepartment(selectedDept).then((r) => setDesignations(r.data));
    }
  }, [selectedDept]);

  useEffect(() => {
    if (isEdit && id) {
      employeeApi.getById(id).then((r) => {
        const e = r.data;
        reset({
          email: e.email,
          firstName: e.firstName,
          lastName: e.lastName,
          phone: e.phone,
          gender: e.gender,
          dateOfBirth: e.dateOfBirth,
          dateOfJoining: e.dateOfJoining,
          address: e.address,
          departmentId: e.department.id,
          designationId: e.designation.id,
        });
      });
    }
  }, [isEdit, id, reset]);

  const onSubmit = async (data: FormData) => {
    setError('');
    setLoading(true);
    try {
      if (isEdit && id) {
        await employeeApi.update(id, data);
      } else {
        await employeeApi.create({
          ...data,
          username: data.email.split('@')[0],
          password: 'Temp@1234',
          role: 'EMPLOYEE',
        });
      }
      navigate('/employees');
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Save failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">
        {isEdit ? 'Edit Employee' : 'Add New Employee'}
      </h1>

      {error && <div className="p-4 bg-red-50 border border-red-200 rounded-xl text-red-700 text-sm">{error}</div>}

      <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <Input label="First Name" error={errors.firstName?.message}
            {...register('firstName', { required: 'Required' })} />
          <Input label="Last Name" error={errors.lastName?.message}
            {...register('lastName', { required: 'Required' })} />
        </div>

        <Input label="Email" type="email" error={errors.email?.message}
          {...register('email', { required: 'Required' })} disabled={isEdit} />

        <div className="grid grid-cols-2 gap-4">
          <Input label="Phone" {...register('phone')} />
          <Input label="Gender" {...register('gender')} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <Input label="Date of Birth" type="date" {...register('dateOfBirth')} />
          <Input label="Date of Joining" type="date" error={errors.dateOfJoining?.message}
            {...register('dateOfJoining', { required: 'Required' })} />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Department</label>
            <select {...register('departmentId', { required: true })}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">Select department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Designation</label>
            <select {...register('designationId', { required: true })}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">Select designation</option>
              {designations.map((d) => <option key={d.id} value={d.id}>{d.title}</option>)}
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
          <textarea {...register('address')} rows={2}
            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-blue-500" />
        </div>

        <div className="flex gap-3 pt-2">
          <Button type="submit" loading={loading}>{isEdit ? 'Save Changes' : 'Create Employee'}</Button>
          <Button type="button" variant="secondary" onClick={() => navigate('/employees')}>Cancel</Button>
        </div>
      </form>
    </div>
  );
}